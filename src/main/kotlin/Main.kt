// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.delay
import org.w3c.dom.Node
import java.awt.Dimension
import kotlin.math.roundToInt

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
    val nodeContainer = mutableStateListOf<NodeObject>()
    Row {
        NodeViewLayout(frameWindowScope.window.size, modifier = Modifier.weight(0.9f, true)) {
            Node(frameWindowScope, NodeObject(NodeTypeStore.getNode(0)))
            nodeContainer.forEach {
                Node(frameWindowScope, it)
            }
        }
        Box(modifier = Modifier.width(5.dp).fillMaxHeight().background(Color.Black))
        Box(modifier = Modifier.padding(5.dp).weight(0.1f)) {
            NodeSelector(nodeContainer)
        }
    }
}

@Composable
fun NodeSelector(nodeContainer: SnapshotStateList<NodeObject>) {
    LazyColumn {
        items(NodeTypeStore.getAllNodes()) { item: NodeType ->
            Button(onClick = {
                nodeContainer.add(NodeObject(item))
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
fun Node(frameWindowScope: FrameWindowScope, node: NodeObject) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val x by remember { mutableStateOf(frameWindowScope.window.size.width / 2) }
    val y by remember { mutableStateOf(frameWindowScope.window.size.height / 2) }
    Column(
        modifier = Modifier.layoutId(IntOffset(x, y)).wrapContentWidth(Alignment.Start)
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }.border(2.dp, Color.Black, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consumeAllChanges()
                  //  if(offsetX+dragAmount.x > ) TODO
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        Text(node.nodeType.name)
    }
}

fun main() = singleWindowApplication {
    App(this)
}