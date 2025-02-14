package com.unciv.ui.mapeditor

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.Json
import com.unciv.Constants
import com.unciv.UnCivGame
import com.unciv.logic.MapSaver
import com.unciv.logic.map.RoadStatus
import com.unciv.models.gamebasics.tr
import com.unciv.ui.saves.Gzip
import com.unciv.ui.utils.onClick
import com.unciv.ui.worldscreen.optionstable.DropBox
import com.unciv.ui.worldscreen.optionstable.PopupTable

class MapEditorOptionsTable(mapEditorScreen: MapEditorScreen): PopupTable(mapEditorScreen){
    init{
        val mapNameEditor = TextField(mapEditorScreen.mapName, skin)
        mapNameEditor.addListener{ mapEditorScreen.mapName=mapNameEditor.text; true }
        add(mapNameEditor).row()
        
        val clearCurrentMapButton = TextButton("Clear current map".tr(),skin)
        clearCurrentMapButton.onClick {
            for(tileGroup in mapEditorScreen.mapHolder.tileGroups)
            {
                val tile = tileGroup.tileInfo
                tile.baseTerrain=Constants.ocean
                tile.terrainFeature=null
                tile.resource=null
                tile.improvement=null
                tile.improvementInProgress=null
                tile.roadStatus=RoadStatus.None
                tile.setTransients()

                tileGroup.update(true,true,true)
            }
        }
        add(clearCurrentMapButton).row()

        val saveMapButton = TextButton("Save".tr(), skin)
        saveMapButton.onClick {
            MapSaver().saveMap(mapEditorScreen.mapName,mapEditorScreen.tileMap)
            UnCivGame.Current.setWorldScreen()
        }
        add(saveMapButton).row()

        val copyMapAsTextButton = TextButton("Copy to clipboard".tr(), skin)
        copyMapAsTextButton.onClick {
            val json = Json().toJson(mapEditorScreen.tileMap)
            val base64Gzip = Gzip.zip(json)
            Gdx.app.clipboard.contents =  base64Gzip
        }
        add(copyMapAsTextButton).row()

        val loadMapButton = TextButton("Load".tr(), skin)
        loadMapButton.onClick { MapScreenLoadTable(mapEditorScreen); remove() }
        add(loadMapButton).row()

        val uploadMapButton = TextButton("Upload".tr(), skin)
        uploadMapButton.onClick {
            val gzippedMap = Gzip.zip(Json().toJson(mapEditorScreen.tileMap))
            DropBox().uploadFile("/Maps/"+mapEditorScreen.mapName,gzippedMap)
        }
        add(uploadMapButton).row()

        val downloadMapButton = TextButton("Download".tr(), skin)
        downloadMapButton.onClick { MapDownloadTable(mapEditorScreen); remove() }
        add(downloadMapButton).row()

        val exitMapEditorButton = TextButton("Exit map editor".tr(), skin)
        exitMapEditorButton.onClick { UnCivGame.Current.setWorldScreen(); mapEditorScreen.dispose() }
        add(exitMapEditorButton ).row()

        val closeOptionsButtton = TextButton("Close".tr(), skin)
        closeOptionsButtton.onClick { remove() }
        add(closeOptionsButtton).row()

        open()
    }
}

class MapDownloadTable(mapEditorScreen: MapEditorScreen):PopupTable(mapEditorScreen){
    init{
        val folderList: DropBox.FolderList
        try{
            folderList = DropBox().getFolderList("/Maps")
            for(downloadableMap in folderList.entries){
                addButton(downloadableMap.name){
                    val mapJsonGzipped = DropBox().downloadFile(downloadableMap.path_display)
                    val decodedMapJson = Gzip.unzip(mapJsonGzipped)
                    val mapObject = MapSaver().mapFromJson(decodedMapJson)
                    MapSaver().saveMap(downloadableMap.name, mapObject)
                    UnCivGame.Current.screen = MapEditorScreen(mapObject)
                }
            }
        }
        catch (ex:Exception){
            addGoodSizedLabel("Could not get list of maps!")
        }
        addButton("Close"){remove()}
        open()
    }
}