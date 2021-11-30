// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Dimension
import java.awt.FileDialog
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.coroutines.coroutineContext

var unconnectedConnection: NodeConnection? = null

@Composable
fun NodeViewLayout(windowSize: Dimension, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map {
            it.measure(constraints)
        }
        layout(constraints.maxWidth, constraints.maxHeight) {
            for (i in placeables.indices) {
                placeables[i].place(((measurables[i].parentData as Modifier) as LayoutIdParentData).layoutId as IntOffset)
            }
        }
    }
}

@Composable
@Preview
fun App(frameWindowScope: FrameWindowScope) {
    MaterialTheme {
        NodeViewport(frameWindowScope)
    }
}

@Composable
fun NodeViewport(frameWindowScope: FrameWindowScope) {
    val nodeContainer =
        mutableStateListOf<NodeObject>(
            NodeObject(
                NodeTypeStore.getNode(4),
                frameWindowScope.window.size.width / 7,
                frameWindowScope.window.size.height / 2
            ), NodeObject(
                NodeTypeStore.getNode(5), frameWindowScope.window.size.width - (frameWindowScope.window.size.width / 4),
                frameWindowScope.window.size.height / 2
            )
        )
    val redrawTrigger = remember { mutableStateOf(false) }
    val nodeRedrawTrigger = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        redrawTrigger.value.let {
            drawConnection(this@Canvas, unconnectedConnection)
            nodeContainer.forEach {
                drawConnection(this@Canvas, it.outputConnector.nodeConnection)
            }
        }
    }
    Row {
        NodeViewLayout(frameWindowScope.window.size, modifier = Modifier.weight(1f, true)) {
            nodeRedrawTrigger.value.let {
                nodeContainer.forEach {
                    Node(nodeContainer, it, redrawTrigger, nodeRedrawTrigger, frameWindowScope, coroutineScope)
                }
            }
        }
        Box(modifier = Modifier.width(5.dp).fillMaxHeight().background(Color.Black))
        Box(modifier = Modifier.padding(5.dp).weight(0.2f)) {
            NodeSelector(frameWindowScope, nodeContainer)
        }
    }
}

fun drawConnection(drawScope: DrawScope, connectionToDraw: NodeConnection?) {
    if (connectionToDraw !== null) {
        drawScope.drawLine(
            Color.Black,
            connectionToDraw.startConnector.offset,
            connectionToDraw.endConnector.offset
        )
    }
}

@Composable
fun NodeSelector(frameWindowScope: FrameWindowScope, nodeContainer: SnapshotStateList<NodeObject>) {
    LazyColumn {
        items(NodeTypeStore.getAllNodes().filter { nodeType -> nodeType.isInList }) { item: NodeType ->
            Button(onClick = {
                nodeContainer.add(NodeObject(item, frameWindowScope.window.size))
            }, modifier = Modifier.clip(CircleShape)) {
                Text(
                    text = item.name,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.size(10.dp))
        }
    }
}

@Composable
fun Node(
    nodeContainer: SnapshotStateList<NodeObject>,
    node: NodeObject,
    redrawTrigger: MutableState<Boolean>,
    nodeRedrawTrigger: MutableState<Boolean>,
    windowScope: FrameWindowScope,
    coroutineScope: CoroutineScope,
) {
    var internalNode by remember { mutableStateOf(node) } // нода этого элемента стэка вызовов компоуза
    var x by remember { mutableStateOf(node.Xpos) }
    var y by remember { mutableStateOf(node.Ypos) }
    val bigImageState = remember { mutableStateOf<ImageBitmap?>(null) }
    val connectorRedrawTrigger = remember { mutableStateOf(false) }
    if (internalNode != node) { // если элемент стэка вызовов компоуза устарел, заменяем его новым
        internalNode = node
        x = internalNode.Xpos
        y = internalNode.Ypos
    }
    Row(
        modifier = Modifier.layoutId(IntOffset(x, y)),
        verticalAlignment = Alignment.CenterVertically
    ) { //Row из трёх колонок - входящих связей, ноды, и исходящих связей
        connectorRedrawTrigger.let {
            Column(modifier = Modifier.offset((-10).dp, 0.dp)) {
                for (i in node.inputConnectors.indices) {
                    NodeConnector(
                        node.inputConnectors[i],
                        node,
                        nodeContainer,
                        redrawTrigger,
                        nodeRedrawTrigger,
                        NodeTypeStore.getNode(node.nodeType.inputNodeList[i]),
                        node.nodeType.inputNameList[i]
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.wrapContentWidth(Alignment.Start).width(125.dp).background(Color.White)
                .border(2.dp, if (node.isError) Color.Red else Color.Black)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consumeAllChanges()
                        x += dragAmount.x.toInt()
                        y += dragAmount.y.toInt()
                        node.Xpos = x
                        node.Ypos = y
                        NeedRedraw(redrawTrigger)
                    }
                }
        ) {
            Text(node.nodeType.name, textAlign = TextAlign.Center)
            Box(modifier = Modifier.height(2.dp).fillMaxWidth().background(Color.Black))
            NodeFields(node, windowScope, bigImageState, coroutineScope, nodeRedrawTrigger)
            if (node.nodeType.isInList) {
                Button(
                    colors = ButtonDefaults.buttonColors(Color.Red),
                    border = BorderStroke(1.dp, Color.Black),
                    onClick = { node.inputConnectors.forEach { it.Remove() }; nodeContainer.remove(node);node.destroy() },
                    modifier = Modifier.defaultMinSize(0.dp, 15.dp).fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Удалить", style = TextStyle(color = Color.White, fontSize = 14.sp))
                }
            }
        }
        connectorRedrawTrigger.let {
            if (node.nodeType.id != 5) {
                Column(modifier = Modifier.offset(10.dp, 0.dp)) {
                    NodeConnector(
                        node.outputConnector,
                        node,
                        nodeContainer,
                        redrawTrigger,
                        nodeRedrawTrigger,
                        NodeTypeStore.getNode(node.nodeType.outputNode),
                        null
                    )
                }
            }
        }
    }
    if (bigImageState.value != null) {
        val img_size = 500.dp
        Image(
            bitmap = bigImageState.value!!,
            contentDescription = "Big Image Viewer",
            modifier = Modifier.layoutId(IntOffset(x, y)).size(img_size).offset(-(img_size / 4), 0.dp),
            alignment = Alignment.CenterStart
        )
    }
}

@Composable
fun NodeConnector(
    nodeConnector: NodeConnector,
    node: NodeObject,
    nodeContainer: SnapshotStateList<NodeObject>,
    canvasRedrawTrigger: MutableState<Boolean>,
    nodeRedrawTrigger: MutableState<Boolean>,
    transferNodeType: NodeType,
    label: String?,
) {
    var connectionRedrawTrigger by remember { mutableStateOf(false) }
    connectionRedrawTrigger.let {
        Text(text = label ?: "Вывод")
        Box(
            modifier = Modifier.clip(shape = CircleShape)
                .background(color = if (nodeConnector.nodeConnection == null) Color.Green else Color.Red).size(25.dp)
                .onGloballyPositioned {
                    nodeConnector.offset = Offset(it.positionInWindow().x + 10, it.positionInWindow().y + 10)
                }
                .pointerInput(Unit) {
                    detectDragGestures(onDragStart = {
                        unconnectedConnection = NodeConnection(
                            node,
                            null,
                            nodeConnector,
                            NodeConnector(nodeConnector.offset, nodeConnector.nodeConnection, true, transferNodeType)
                        )
                        nodeConnector.Remove()
                        nodeConnector.nodeConnection = unconnectedConnection
                        connectionRedrawTrigger = !connectionRedrawTrigger
                    }, onDrag = { change, _ ->
                        if (unconnectedConnection != null) {
                            var connected = false
                            unconnectedConnection!!.endConnector.offset =
                                unconnectedConnection!!.endConnector.offset.plus(change.positionChange())
                            if (nodeConnector.isInput) {
                                for (curNode in nodeContainer) {
                                    if (curNode == node || curNode.nodeType.outputNode != transferNodeType.id) continue
                                    if (unconnectedConnection!!.endConnector.offset.isInBounds(curNode.outputConnector)) {
                                        unconnectedConnection!!.startConnector = curNode.outputConnector
                                        unconnectedConnection!!.endConnector = nodeConnector
                                        unconnectedConnection!!.startNodeObject = curNode
                                        unconnectedConnection!!.endNodeObject = node
                                        curNode.outputConnector.nodeConnection = unconnectedConnection
                                        unconnectedConnection = null
                                        connected = true
                                        break
                                    }
                                }
                            } else {
                                outer@ for (curNode in nodeContainer) {
                                    if (curNode == node) continue
                                    for (con in curNode.inputConnectors) {
                                        if (con.transferNodeType != transferNodeType) continue
                                        if (unconnectedConnection!!.endConnector.offset.isInBounds(con)) {
                                            if (con.nodeConnection != null) con.Remove()
                                            unconnectedConnection!!.endConnector = con
                                            unconnectedConnection!!.endNodeObject = curNode
                                            con.nodeConnection = unconnectedConnection
                                            unconnectedConnection = null
                                            connected = true
                                            break@outer
                                        }
                                    }
                                }
                            }
                            if (connected) {
                                node.invalidateInput(true)
                                NeedRedraw(nodeRedrawTrigger)
                            }
                        }
                        NeedRedraw(canvasRedrawTrigger)
                    }, onDragEnd = {
                        if (unconnectedConnection != null) {
                            nodeConnector.nodeConnection = null
                            unconnectedConnection = null
                            connectionRedrawTrigger = !connectionRedrawTrigger
                            NeedRedraw(canvasRedrawTrigger)
                        }
                    })
                })
        Text(text = transferNodeType.name)
    }

}

fun NeedRedraw(vararg redrawTrigger: MutableState<Boolean>) {
    redrawTrigger.forEach {
        it.value = !it.value
    }
}

@Composable
fun NodeFields(
    node: NodeObject,
    windowScope: FrameWindowScope,
    bigImageHandler: MutableState<ImageBitmap?>,
    coroutineScope: CoroutineScope,
    nodeRedrawTrigger: MutableState<Boolean>
) {
    for (i in node.content.indices) {
        val fieldValue = node.content[i]
        Spacer(modifier = Modifier.height(10.dp))
        if (fieldValue is Int || fieldValue is Float || fieldValue is String) {
            var text by remember { mutableStateOf(TextFieldValue(fieldValue.toString())) }
            var internalNode by remember { mutableStateOf(node) } // см. internalNode в Node()
            if (node != internalNode) {
                internalNode = node
                text = TextFieldValue(fieldValue.toString())
            }
            OutlinedTextField(
                modifier = Modifier.padding(5.dp),
                value = text,
                onValueChange = {
                    val temp = node.content[i]
                    if (fieldValue is Int) {
                        node.content[i] = it.text.toIntOrNull() ?: node.content[i]
                    } else if (fieldValue is Float) {
                        node.content[i] = it.text.toFloatOrNull() ?: node.content[i]
                    } else {
                        node.content[i] = it.text
                    }
                    text = it
                    if (temp != node.content[i]) node.invalidateInput()
                },
                label = { Text("Значение") })
        } else {
            var windowOpened by remember { mutableStateOf<Boolean?>(null) }
            val hoverInteraction = remember { MutableInteractionSource() }
            val isHovered by hoverInteraction.collectIsHoveredAsState()
            if (windowOpened != null) {
                windowScope.FileDialog(
                    if (windowOpened!!) "Выберите файл для открытия" else "Выберите файл для сохранения",
                    windowOpened!!
                ) {
                    val windowTypeCache = windowOpened
                    coroutineScope.launch(Dispatchers.IO) {
                        if (it != null) {
                            try {
                                if (windowTypeCache!!) {
                                    val img = ImageIO.read(it.toFile())
                                    node.content[i] = img.toComposeImageBitmap()
                                } else {
                                    ImageIO.write((node.output as ImageBitmap).toAwtImage(), "png", it.toFile())
                                }
                                node.invalidateInput(false)
                                NeedRedraw(nodeRedrawTrigger)
                            } catch (_: IOException) {
                            }
                        }
                    }
                    windowOpened = null
                }
            }
            if (isHovered) bigImageHandler.value = node.output as ImageBitmap?
            else bigImageHandler.value = null
            if (node.output != null) {
                Image(
                    node.output as ImageBitmap,
                    contentDescription = "Image viewer",
                    modifier = Modifier.size(100.dp).hoverable(
                        hoverInteraction
                    )
                )
            }
            if (node.nodeType.miscAttribute == ImageView.Openable) {
                Button({ windowOpened = true }) { Text("Открыть") }
            } else if (node.nodeType.miscAttribute == ImageView.Saveable) {
                Button({ windowOpened = false }) { Text("Сохранить") }
            }
        }
    }
}

@Composable
fun FrameWindowScope.FileDialog(
    title: String,
    isLoad: Boolean,
    onResult: (result: java.nio.file.Path?) -> Unit
) = AwtWindow(
    create = {
        object : FileDialog(window, "Выберите файл", if (isLoad) LOAD else SAVE) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    if (file != null) {
                        onResult(File(directory).resolve(file).toPath())
                    } else {
                        onResult(null)
                    }
                }
            }
        }.apply {
            this.title = title
        }
    },
    dispose = FileDialog::dispose
)

fun main() {
    nu.pattern.OpenCV.loadLocally()
    singleWindowApplication {
        App(this)
    }
}