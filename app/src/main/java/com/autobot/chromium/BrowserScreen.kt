package com.autobot.chromium

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autobot.chromium.theme.MyAppThemeColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(viewModel: BrowserViewModel) {
    var searchText by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val tabs by remember { mutableStateOf(viewModel.tabs) }
    var newTabUrl by remember { mutableStateOf("") }
    var currentTabUrl by remember { mutableStateOf("") }

    LaunchedEffect(newTabUrl) {
        if (newTabUrl.isNotBlank()) {
            viewModel.addTab(newTabUrl)
            newTabUrl = ""
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Row
        TabRow(modifier = Modifier.height(70.dp), selectedTabIndex = viewModel.selectedTabIndex) {
            tabs.forEachIndexed { index, tabState ->
                Tab(
                    modifier = Modifier.border(width = 1.dp, color = Color.Gray),
                    selected = viewModel.selectedTabIndex == index,
                    onClick = {
                        viewModel.selectTab(index)
                        currentTabUrl = tabState.url
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(
                                modifier = Modifier,
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = tabState.url, color = Color.Black,modifier = Modifier, maxLines = 1) // Tab Text
                            }
                            Column(
                                modifier = Modifier,
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.End
                            ) {
                                IconButton(onClick = {
                                    viewModel.closeTab(index) // Close tab when cross is clicked
                                }) {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        imageVector = Icons.Default.Clear, // Use your close icon
                                        contentDescription = "Close Tab",
                                        tint = Color.Red // Color for close icon
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        Row(modifier = Modifier.weight(1f)) {
            if (isSearching) {
                BrowserWithTabs(searchText, isSearching = false)
            } else {
                if (currentTabUrl.isBlank()) {
                    BrowserHomePage() // Shows the homepage if no search text
                } else {
                    BrowserWithTabs(currentTabUrl, isSearching = true)
                }
            }
        }

        SearchBar(
            searchText = searchText,
            onTextChange = { searchText = it },
            onAddTab = { newTabUrl = "Home" },
            onMenuClick = {
                scope.launch {
                    showBottomSheet = true
                }
            },
            onSearch = { query ->
                searchText = query
                isSearching = true
                currentTabUrl = query
            }
        )
    }

    // Modal Bottom Sheet
    if (showBottomSheet) {
        androidx.compose.material3.ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                scope.launch {
                    showBottomSheet = false
                }
            }
        ) {
            BottomSheetContent()
        }
    }
}
@Composable
fun SearchBar(
    searchText: String,
    onTextChange: (String) -> Unit,
    onAddTab: () -> Unit,
    onMenuClick: () -> Unit,
    onSearch: (String) -> Unit // New parameter to handle search submission
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(
        color = MyAppThemeColors.current.tertiaryDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .background(
                            color = MyAppThemeColors.current.tertiaryDark,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Image(
                        painter =
                        painterResource(id = R.drawable.ic_google),
                        contentDescription = "More Options",
                        modifier = Modifier
                            .padding(8.dp)
                            .background(color = MyAppThemeColors.current.tertiary)
                    )
                    BasicTextField(
                        value = searchText,
                        onValueChange = onTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Search  // Set IME action to "Search"
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide()  // Hide the keyboard
                                onSearch(searchText)  // Trigger search when "Search" key is pressed
                            }
                        ),
                        singleLine = true  // Ensure it stays single-line
                    )
                }
            }

            // Plus icon and Menu icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = onAddTab) {
                    Icon(Icons.Default.Add, contentDescription = "Add New Tab")
                }

                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                }
            }
        }
    }
}


@Preview
@Composable
fun BottomSheetContent() {
Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 4.dp, vertical = 16.dp)
            .background(
                color = MyAppThemeColors.current.tertiaryDark,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 4.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        // First Row of icons
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconColumn(imageVector = Icons.Outlined.Star, label = "Website")
            IconColumn(painter = painterResource(id = R.drawable.ic_download), label = "Download")
            IconColumn(painter = painterResource(id = R.drawable.ic_recent), label = "Recent")
            IconColumn(painter = painterResource(id = R.drawable.ic_settings), label = "Settings")
        }

        androidx.compose.material3.HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = MyAppThemeColors.current.myText
        )
        // Second Row of icons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconColumn(painter = painterResource(id = R.drawable.ic_exit), label = "Exit")
            IconColumn(painter = painterResource(id = R.drawable.ic_incognito), label = "Incognito")
            IconColumn(painter = painterResource(id = R.drawable.ic_share), label = "Share")
            IconColumn(painter = painterResource(id = R.drawable.ic_dark_mode), label = "Dark Mode")
        }

        // Third Row of icons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconColumn(painter = painterResource(id = R.drawable.ic_desktop_mode), label = "Desktop Mode")
            IconColumn(painter = painterResource(id = R.drawable.ic_help_feedback), label = "Help")
            IconColumn(painter = painterResource(id = R.drawable.ic_find_onpage), label = "Find")
            IconColumn(painter = painterResource(id = R.drawable.ic_delete), label = "Delete")
        }
    }
}

@Composable
fun IconColumn(
    imageVector: ImageVector? = null,
    painter: Painter? = null,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { }
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (imageVector != null) {
            Image(
                imageVector = imageVector,
                contentDescription = label,
                modifier = Modifier.size(40.dp)
            )
        } else if (painter != null) {
            Image(
                painter = painter,
                contentDescription = label,
                modifier = Modifier.size(50.dp)
            )
        }
        Text(label, fontSize = 16.sp)
    }
}


@Preview
@Composable
fun BrowserHomePage() {
    // Replace R.drawable.your_background_image with your actual image resource
    val backgroundImage = painterResource(id = R.drawable.bg)

    Box(
        modifier = Modifier.wrapContentHeight()
    ) {
        // Background image
        Image(
            painter = backgroundImage,
            contentDescription = null,
            contentScale = ContentScale.Crop, // Adjusts the image scaling
            modifier = Modifier.fillMaxSize()
        )

        // Overlay content
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 16.dp)// Adjust padding as needed
        ) {
            BrowserHome()
            VoiceSearchBar(modifier = Modifier.weight(1f)) { }
            RecentWebsites()
        }
    }
}