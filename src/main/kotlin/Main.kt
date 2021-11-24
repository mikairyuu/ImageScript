// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutIdParentData
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.singleWindowApplication
import java.awt.Dimension
import kotlin.math.roundToInt
import kotlin.reflect.typeOf

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
        mutableStateListOf<NodeObject>(NodeObject(NodeTypeStore.getNode(0), frameWindowScope.window.size))
    Row {
        NodeViewLayout(frameWindowScope.window.size, modifier = Modifier.weight(0.9f, true)) {
            nodeContainer.forEach {
                Node(nodeContainer, it)
            }
        }
        Box(modifier = Modifier.width(5.dp).fillMaxHeight().background(Color.Black))
        Box(modifier = Modifier.padding(5.dp).weight(0.1f)) {
            NodeSelector(frameWindowScope, nodeContainer)
        }
    }
}

@Composable
fun NodeSelector(frameWindowScope: FrameWindowScope, nodeContainer: SnapshotStateList<NodeObject>) {
    LazyColumn {
        items(NodeTypeStore.getAllNodes()) { item: NodeType ->
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
fun Node(nodeContainer: SnapshotStateList<NodeObject>, node: NodeObject) {
    var internalNode by remember { mutableStateOf(node) } // нода этого элемента стэка вызовов компоуза
    var x by remember { mutableStateOf(node.Xpos) }
    var y by remember { mutableStateOf(node.Ypos) }
    if (internalNode != node) { // если элемент стэка вызовов компоуза устарел, заменяем его новым
        internalNode = node
        x = internalNode.Xpos
        y = internalNode.Ypos
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.layoutId(IntOffset(x, y)).wrapContentWidth(Alignment.Start).width(125.dp)
            .border(2.dp, Color.Black)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consumeAllChanges()
                    //  if(offsetX+dragAmount.x > ) TODO
                    x += dragAmount.x.toInt()
                    y += dragAmount.y.toInt()
                    node.Xpos = x
                    node.Ypos = y
                }
            }
    ) {
        Text(node.nodeType.name, textAlign = TextAlign.Center)
        Box(modifier = Modifier.height(2.dp).fillMaxWidth().background(Color.Black))
        NodeFields(node)
        Button(
            colors = ButtonDefaults.buttonColors(Color.Red),
            border = BorderStroke(1.dp, Color.Black),
            onClick = { nodeContainer.remove(node) },
            modifier = Modifier.defaultMinSize(0.dp, 15.dp).fillMaxWidth(),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("Удалить", style = TextStyle(color = Color.White, fontSize = 14.sp))
        }
    }
}

@Composable
fun NodeFields(node: NodeObject) {
    for(i in node.content.indices){
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
                    if (fieldValue is Int) {
                        if (it.text.toIntOrNull() == null && it.text.isNotEmpty()) {
                            return@OutlinedTextField
                        }
                    } else if (fieldValue is Float) {
                        if (it.text.toFloatOrNull() == null && it.text.isNotEmpty()) {
                            return@OutlinedTextField
                        }
                    }
                    text = it
                    node.content[i] = it.text
                },
                label = { Text("Значение") })
        } else {

        }
    }
}

fun main() = singleWindowApplication {
    App(this)
}