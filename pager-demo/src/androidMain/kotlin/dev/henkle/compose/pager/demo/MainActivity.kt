package dev.henkle.compose.pager.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import dev.henkle.compose.paging.MappedTransformedData
import dev.henkle.compose.paging.PagedLazyColumn
import dev.henkle.compose.paging.TransformedItem
import dev.henkle.compose.paging.rememberPagedLazyColumnController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val exampleData = MutableList(9) { it.toString() }
        val pageSize = 9
        val maxPages = 3

        val transform: (
            pages: IntRange,
            data: List<String>,
        ) -> MappedTransformedData<String, String> = transform@{ pages, items ->
            if (pages.isEmpty()) return@transform MappedTransformedData()

            val data = items.map { TransformedItem(item = "item $it", original = it) }.toMutableList()
            val fullPages = data.size / pageSize
            val partialPage = data.size % pageSize != 0
            val sectionCount = if (partialPage) fullPages + 1 else fullPages - 1
            val firstIndexOfLastSection = (sectionCount - if (partialPage) 1 else 0) * pageSize
            var currentPage = pages.last
            for (index in firstIndexOfLastSection downTo 0 step pageSize) {
                data.add(index, TransformedItem(item = "Start of page $currentPage"))
                currentPage -= 1
            }

            val sizeChange = sectionCount.coerceAtLeast(0)
            MappedTransformedData(
                items = data,
                totalSizeChange = sizeChange,
                pageSizeChanges = List(size = sizeChange) { 1 },
            )
        }

        val reverseTransform: (item: String) -> String = { it }

        setContent {
            val controller = rememberPagedLazyColumnController<String>()
            val scope = rememberCoroutineScope { Dispatchers.Main }

            LaunchedEffect(Unit) {
                for (i in 0..<exampleData.size) {
                    delay(1_000)
                    Logger.e("garrison") { "updating item $i" }
                    controller.update(item = "item $i", newValue = (-i * i).toString())
                    Logger.e("garrison") { "item $i updated" }
//                    controller.delete(item = "item $i")
                }
//                delay(1_000)
//                controller.delete(item = "-4")
//                delay(5_000)
//                controller.delete(item = "-36")
            }

            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xffeaeaea)),
            ) {
                PagedLazyColumn(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    controller = controller,
                    loadingCirclesEnabled = true,
                    pageSize = pageSize,
                    maxPages = maxPages,
                    key = { it },
                    transform = transform,
                    reverseTransform = reverseTransform,
                    fetch = { offset, pageSize ->
                        delay(500)
                        when {
                            offset >= exampleData.size -> emptyList()
                            offset + pageSize >= exampleData.size ->
                                exampleData.subList(offset, exampleData.size)
                            else -> exampleData.subList(offset, offset + pageSize)
                        }
                    },
                ) { text ->
                    Surface(
                        modifier =
                        Modifier
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .fillMaxWidth(),
                        color = Color.White,
                        elevation = 2.dp,
                        shape = RoundedCornerShape(size = 10.dp),
                    ) {
                        Box(
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = text)
                        }
                    }
                }

//                PagedLazyColumn(
//                    modifier =
//                    Modifier
//                        .fillMaxWidth()
//                        .weight(1f),
//                    loadingCirclesEnabled = true,
//                    pageSize = pageSize,
//                    maxPages = maxPages,
//                    key = { it },
//                    getID = { it },
//                    transform = transform,
//                    fetch = { lastID, pageSize ->
//                        delay(500)
//                        val index =
//                            lastID?.let {
//                                exampleData.indexOf(lastID)
//                                    .takeIf { it != -1 }
//                                    ?.let { it + 1 }
//                            }
//                        when {
//                            index == null -> exampleData.subList(0, pageSize)
//                            index >= exampleData.size -> emptyList()
//                            index + pageSize >= exampleData.size ->
//                                exampleData.subList(index, exampleData.size)
//                            else -> exampleData.subList(index, index + pageSize)
//                        }
//                    },
//                ) { text ->
//                    Surface(
//                        modifier =
//                        Modifier
//                            .padding(horizontal = 10.dp, vertical = 5.dp)
//                            .fillMaxWidth(),
//                        color = Color.White,
//                        elevation = 2.dp,
//                        shape = RoundedCornerShape(size = 10.dp),
//                    ) {
//                        Box(
//                            modifier =
//                            Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 20.dp),
//                            contentAlignment = Alignment.Center,
//                        ) {
//                            Text(text = text)
//                        }
//                    }
//                }

                Button(
                    modifier =
                    Modifier
                        .padding(horizontal = 10.dp)
                        .height(height = 48.dp)
                        .fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            for (i in -1 downTo -10) {
                                exampleData.add(0, i.toString())
                            }
                            controller.refresh()
                        }
                    },
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Refresh")
                    }
                }
            }
        }
    }
}
