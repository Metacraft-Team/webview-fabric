![fabric](https://user-images.githubusercontent.com/5381613/187888522-ba4f933e-b5b3-4f51-b95e-401b888789d8.png) ➕ ![187888006-70d40fe7-1cad-4962-a4dd-c94ee18b9835](https://user-images.githubusercontent.com/5381613/187888698-75aba4dd-78bb-41c6-ae59-611fd5a56c45.png)

# Webview for Minecraft Fabric
[![Website](https://img.shields.io/website?down_message=offline&style=for-the-badge&up_color=blue&up_message=online&url=https%3A%2F%2Fmetacraft.cc)](https://metacraft.cc)
[![license](https://img.shields.io/github/license/Metacraft-Team/NFT-fabric?style=for-the-badge)](https://github.com/Metacraft-Team/metacraft-client/blob/feature/webviewWith95/LICENSE)
[![Discord](https://img.shields.io/discord/881890111644631122?label=Discord&style=for-the-badge)](http://discord.gg/yEv3qKhVBH)
[![Twitter Follow](https://img.shields.io/twitter/follow/MetacraftCC?color=green&logoColor=green&style=for-the-badge&label=Twitter)](https://twitter.com/MetacraftCC)
[![YouTube Channel Views](https://img.shields.io/youtube/channel/views/UC-fAgQr5lxNVZU4_LVXmKOg?style=for-the-badge&label=Youtube%20Views)](https://www.youtube.com/channel/UC-fAgQr5lxNVZU4_LVXmKOg)

## What is this
It can open embedded web browser in Minecraft. This was initialy made for [Metacraft](https://metacraft.cc/)

it looks like [MCEF](https://github.com/montoyo/mcef), what is the difference: this mode is working on fabric with higher Chromium Embedded Framework version.

You can view example like this: [video](https://twitter.com/metacraftcc/status/1530559870121803776?s=21&t=w_Y275hrBrboEbmDbyV9-Q)


## Currently supported platforms
- Windows 10/11 64 bit
- Macos frequently crashes

## Test

1. Unzip the file tha named of '95.zip' to the run directory
2. Make sure there is 'jcef.dll' file in this directory
3. Now run the fabric client
4. Enter the game normally
5. Press the V key to jump out of the browser interface
6. You will get a browser in your minecraft game, like this:
![7c67b3e09a8b2895fccca96864238b2](https://user-images.githubusercontent.com/90142475/186363451-3374458d-5694-48a2-b893-8cbcd0da7285.png)


## Development
1. You can customize browser-triggered actions (eg `keyPressed`,`keyReleased`,`mouseClicked` ) to accomplish various things through rewrite code in BrowserScreen class
2. org.cef.browser.CefRenderer.onPaint will render the browser screen.

## Compiler cef
You can refer to this official link: [Cef build](https://bitbucket.org/chromiumembedded/java-cef/wiki/BranchesAndBuilding)



