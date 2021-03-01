/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import com.example.androiddevchallenge.ui.theme.MyTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                MyApp()
            }
        }
    }
}

// Start building your app here!
@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.PuppysScreen.route
    ) {
        composable(Screen.PuppysScreen.route) { PuppysScreen(navController) }
        composable("${Screen.PuppyScreen.route}/{index}") {
            PuppyScreen(navController, puppyData[it.arguments?.getString("index")?.toInt() ?: 0])
        }
    }
}

sealed class Screen(val route: String) {
    object PuppysScreen : Screen("puppys")
    object PuppyScreen : Screen("puppy")
}

@Composable
fun PuppysScreen(navController: NavHostController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Puppy") }) },
        content = { PuppyGrid(navController) }
    )
}

@Composable
fun PuppyGrid(
    navController: NavHostController,
    data: List<Puppy> = puppyData,
    columns: Int = 2
) {
    LazyColumn(
        modifier = Modifier,
        contentPadding = PaddingValues(8.dp),
        content = {
            itemsIndexed(data) { index, _ ->
                Row {
                    for (columnIndex in 0 until columns) {
                        val itemIndex = index * columns + columnIndex
                        if (itemIndex < data.size) ItemPuppy(
                            puppy = data[itemIndex],
                            modifier = Modifier
                                .padding(8.dp)
                                .weight(weight = 1f, fill = true),
                            onClick = { navController.navigate("${Screen.PuppyScreen.route}/$itemIndex") }
                        )
                        else Spacer(Modifier.weight(weight = 1f, fill = true))
                    }
                }
            }
        }
    )
}

@Composable
fun ItemPuppy(
    modifier: Modifier = Modifier,
    puppy: Puppy = Puppy(nickName = puppyPhotos.first().first, photo = puppyPhotos.first().second),
    onClick: () -> Unit = {}
) {
    var isTouchMe by remember { mutableStateOf(false) }
    val transition = updateTransition(targetState = isTouchMe)
    val scale by transition.animateFloat { if (it) 1.03F else 1F }
    val backgroundColor by transition.animateColor {
        if (it) Color.LightGray else MaterialTheme.colors.background
    }
    val painter = painterResource(id = puppy.photo)
    Card(
        modifier = modifier
            .scale(scale)
            .pointerInteropFilter {
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> isTouchMe = true
                    MotionEvent.ACTION_UP -> {
                        if (isTouchMe) onClick()
                        isTouchMe = false
                    }
                    MotionEvent.ACTION_CANCEL -> isTouchMe = false
                }
                true
            },
        backgroundColor = backgroundColor,
        elevation = 5.dp
    ) {
        Column {
            Image(
                painter = painter, contentDescription = "PuppyPhoto",
                modifier = Modifier
                    .height(160.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Text(
                text = puppy.nickName,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 8.dp),
                maxLines = 1,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = puppy.otherInfo,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(8.dp),
                maxLines = 2
            )
        }
    }
}

@Composable
fun PuppyScreen(navController: NavHostController, puppy: Puppy) {
    Scaffold(
        topBar = { PuppyScreenContentTopAppbar { navController.popBackStack() } },
        floatingActionButton = { PuppyScreenContentFloatingActionButton() },
        content = { PuppyScreenContent(puppy) }
    )
}

@Composable
fun PuppyScreenContentTopAppbar(onClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = "PuppyDetails") },
        navigationIcon = {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "back",
                modifier = Modifier.clickable(onClick = onClick)
            )
        }
    )
}

@Composable
fun PuppyScreenContentFloatingActionButton() {
    FloatingActionButton(onClick = { }, modifier = Modifier) {
        Row(modifier = Modifier.padding(start = 8.dp, end = 8.dp)) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Adopt Me")
            Text(text = "Adopt Me", style = MaterialTheme.typography.h6)
        }
    }
}

@Composable
fun PuppyScreenContent(
    puppy: Puppy = Puppy(
        nickName = puppyPhotos.first().first,
        photo = puppyPhotos.first().second
    )
) {
    val painter = painterResource(id = puppy.photo)
    val scrollState = ScrollState(0)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(state = scrollState)
    ) {
        Image(
            painter = painter, contentDescription = "PuppyPhoto",
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .padding(top = 420.dp, bottom = 80.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .shadow(5.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(MaterialTheme.colors.background)
        ) {
            Row {
                Text(
                    text = puppy.nickName,
                    style = MaterialTheme.typography.h4,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 8.dp),
                    maxLines = 1,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "(${puppy.breed})",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.Bottom)
                )
            }
            Row(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "Sex:",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.align(Alignment.Bottom),
                )
                Text(
                    text = if (puppy.sex) "Dog" else "Bitch",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.Bottom)
                )
            }
            Row(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "Age:",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.align(Alignment.Bottom),
                )
                Text(
                    text = "${puppy.age} Years Old.",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.Bottom)
                )
            }
            Text(
                text = "Info:",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 8.dp),
            )
            Text(
                text = puppy.info,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(8.dp),
            )
            Text(
                text = "OtherInfo:",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 8.dp),
            )
            Text(
                text = puppy.otherInfo,
                modifier = Modifier.padding(8.dp),
                lineHeight = 20.sp,
                style = MaterialTheme.typography.body1,
            )
        }
    }
}

val puppyData by lazy {
    List(puppyPhotos.size) {
        Puppy(
            nickName = puppyPhotos[it].first,
            photo = puppyPhotos[it].second
        )
    }
}

val puppyPhotos =
    arrayOf(
        "Jerry." to R.mipmap.img_puppy_01,
        "Teddy." to R.mipmap.img_puppy_02,
        "Bella." to R.mipmap.img_puppy_03,
        "Lucy." to R.mipmap.img_puppy_04,
        "Rocky." to R.mipmap.img_puppy_05,
        "Maggie." to R.mipmap.img_puppy_06,
        "Jake." to R.mipmap.img_puppy_07,
        "Molly." to R.mipmap.img_puppy_08,
        "Daisy." to R.mipmap.img_puppy_09,
        "Rain." to R.mipmap.img_puppy_10,
        "Dolly." to R.mipmap.img_puppy_11,
        "Rose." to R.mipmap.img_puppy_12,
        "Eyre." to R.mipmap.img_puppy_13,
        "Hiiro." to R.mipmap.img_puppy_14,
        "HanHan." to R.mipmap.img_puppy_15,
    )

data class Puppy(
    val nickName: String,
    @DrawableRes val photo: Int,
    val breed: String = "Poodle.",
    val sex: Boolean = true,
    val age: Int = 2,
    val info: String =
        """
            Can get along with children.

            Get along with cats.

            Can get along with dogs.

            Go out for a walk every day.

            Know how to defecate in designated places.

            Know how to listen to simple instructions.

            Sterilized.
        """.trimIndent(),
    val otherInfo: String = "\"$nickName\", a $age-year-old lady, was sent to our adoption center today. It turned out that his original owner had not taken good care of him about half a year ago. He was often hairy and filthy. He had been put in an outdoor cage for a long time. Some kind-hearted people asked his original owner to abandon him and wanted to take care of him! After \"$nickName\" had been spoiled for half a year, problems began to occur, Since the kind-hearted person is also an elderly person and does not know how to teach him, the current owner has been bitten many times by $nickName. $nickName is a very close child who can get along with cats and dogs. The current owner hopes to find a family for him who can teach him. If the adopter intends to adopt $nickName, he must also pay attention to his behavior! Prospective adopter, Must be at least 25 years old, if you live in a place where you can't keep dogs, no application will be accepted!"
)

@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp()
    }
}

@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        MyApp()
    }
}

@Preview("ItemPuppy")
@Composable
fun ItemPuppyPreview() {
    MyTheme {
        ItemPuppy()
    }
}

@Preview("PuppyScreenContent")
@Composable
fun PuppyScreenContentPreview() {
    MyTheme {
        PuppyScreenContent()
    }
}
