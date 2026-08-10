//var server = "./MapService";
//var server = "http://192.168.100.209:8070/map/MapService";
var server = "./ver1/mapservice";

var nowTileset;
var nowTile;

var key = "981edekdi1298d12ddk121";

var resolutions =
	[
		2088.96,
		1044.48,
		522.24,
		261.12,
		130.56,
		65.28,
		32.64,
		16.32,
		8.16,
		4.08,
		2.04,
		1.02,
		0.51,
		0.255
	];
// 기본 맵 생성
proj4.defs("EPSG:5179", "+proj=tmerc +lat_0=38 +lon_0=127.5 +k=0.9996 +x_0=1000000 +y_0=2000000 +ellps=GRS80 +units=m +no_defs");
// OpenLayers에서 EPSG:5179 좌표계 등록
ol.proj.proj4.register(proj4);

var epsg_5179 = ol.proj.get('EPSG:5179');

epsg_5179.setExtent([-200000.0, -28024123.62, 31824123.62, 4000000.0]);


/*
var vworldLayer = new ol.layer.Tile({
	source: new ol.source.XYZ({
		projection: 'EPSG:5179',
		tileGrid: new ol.tilegrid.WMTS({
			tileSize: [256, 256],
			origin: ol.extent.getTopLeft(epsg_5179.getExtent()),
			resolutions: resolutions
		}),
		wrapX: true,
		crossOrigin: 'anonymous',

		tileUrlFunction: function(coordinate) {

			var z = coordinate[0] + 1;
			var x = coordinate[1];
			var y = coordinate[2];

			url = server + "?req=timg&timg=emap/emp01/" + z + "/" + x + '/' + y + '.png';
			return url;
		}
	})
});
*/


var map;

/**
 * Map 객체 초기화
 */
function init(){
	getTileList();

	var emaplayer = new ol.layer.Tile({
		source: constructSourceEmap(nowTileset, nowTile)
		//source: constructSource('smggis:mb_b_ld')
	});

	emaplayer.set('name', nowTileset+'_'+nowTile);

	map = new ol.Map({
		target: 'map',
		layers: [emaplayer],
		view: new ol.View({
			center: [953919, 1952040],
			resolutions: resolutions,
			maxZoom: 13,
			minZoom: 0,
			maxResolution: 2088.96,
			minResolution: 0.255,
			projection: 'EPSG:5179',
			constrainResolution: true,
			zoom: 0
		}),
	
	});
	
	view = map.getView();
    view.on('change:center', logCenterAndZoom);
	view.on('change:resolution', logCenterAndZoom);
	logCenterAndZoom();
}


function constructSourceEmap(tileset, tile) {

	var source = new ol.source.XYZ({
		projection: 'EPSG:5179',
		tileGrid: new ol.tilegrid.WMTS({
			//tileSize: [256, 256],
			tileSize: [256, 256],
			origin: ol.extent.getTopLeft(epsg_5179.getExtent()),
			resolutions: resolutions
		}),
		wrapX: false,
		crossOrigin: 'anonymous',
		tilePixelRatio: 2,
		tileUrlFunction: function(coordinate) {

			var z = coordinate[0] + 1;
			//var z = coordinate[0];
			var x = coordinate[1];
			var y = coordinate[2];

			url = server + "?req=tile&path="+tileset+"/"+tile+"/" + z + "/" + x + '/' + y + '.png'+'&preview='+ new Date().getTime()+"&key="+key;
			return url;
		}
	});

	return source;
}

function getTileList(){
	
	var jsonurl = server+"?req=tiles&key="+key;
	var params = {
		from: 1,
		to: 10
	};


	var html ='<button style="margin-top:5px; height:25px; width: 200px;" onclick="reloadDesign()">주제도디자인변경업데이트</button><br>';
	    html+='레벨<input style="margin-top:5px; height:25px; width: 25px;" id="level" value="1"/><br>';
	    html+='중심좌표<input style="margin-top:5px; height:25px; width: 150px;" id="coord" value="1"/><br>';	
	    html+='<button style="margin-top:5px; height:25px; width: 200px;" onclick="moveToCenterAndZoom()">지도이동</button><br>';
	    html+='영역<input style="margin-top:5px; height:25px; width: 230px;" id="mbr" value="1" placeholder="xmin, ymin, xmax, ymax"/><br>';	
	
	html+='주제도와 타일 리스트<br>';
	
	$.ajax ({
		url: jsonurl,
		dataType: "json",
		async: false,
		success: function(responseData) {

			$.each(responseData.body, function(i, item) { 

				for(var k=0; k<item.length; k++){
					console.log(i+item[k].name);
					html+=item[k].name;
					for(var n=0; n<item[k].tile.length; n++){
						if(n==0 && k==0){
							nowTileset = item[k].name;
							nowTile = item[k].tile[n];
						}				
						html+='<button style="margin-top:5px; height:25px; width: 80px;" onclick="setMainTile('+'\''+item[k].name+'\''+','+'\''+item[k].tile[n]+'\''+')">'+item[k].tile[n]+'</button>';
						//console.log(i+item[k].tile[n]);
					}

					html+='<br>';
				}


			});
			$("#left").empty();
			$("#left").append(html);
	
		}
	})
	
}

// 중심 좌표와 줌 레벨 input의 값으로 지도 이동
function moveToCenterAndZoom() {
    // 입력된 중심 좌표를 가져와 경도와 위도로 분리
    
    const levelInput = document.getElementById('level');
    const coordInput = document.getElementById('coord');
    
    const centerValue = coordInput.value.split(',');
    const x = parseFloat(centerValue[0]);
    const y = parseFloat(centerValue[1]);
    const zoom = parseFloat(levelInput.value);

    // 유효성 검사 후 지도 이동
    if (!isNaN(x) && !isNaN(y)) {
        
        view.setCenter([x,y]); // 중심 좌표 설정
    }
    if (!isNaN(zoom)) {
        view.setZoom(zoom-1); // 줌 레벨 설정
    }
}


function setMainTile(tileset, tile){
	nowTileset = tileset;
	nowTile = tile;
	setLayer(nowTileset, nowTile);
}


/**
 * 
 * @param {int} idx 선택된 레이어 인덱스
 */
function setLayer(tileset, tile){

	var geolayer = new ol.layer.Tile({
		source: constructSourceEmap(tileset, tile)
	});
	//레이어를 구분할 수 있는 Key 로 'name'을 등록
	geolayer.set('name', tileset+'_'+tile);

	var lays = map.getLayers().getArray();
	for(var i=0; i < lays.length; i++){
		var lay = lays[i];
		map.removeLayer(lay);
	}


	//map 객체에 레이어 등록
	map.addLayer(geolayer);
	
	//map 현재 지도 영역 화명 갱신
	map.updateSize();
}

function reloadtile(){

	var jsonurl = server+"?req=updatetile";
	var params = {
		version: "test",
	};
	
	$.ajax ({
		url: jsonurl,
		dataType: "json",
		async: false,
		success: function(responseData) {
		}
	})
	
	//source.clear();

	map.updateSize();
}

function reloadDesign(){

	var jsonurl = server+"?req=update";
	var params = {
		version: "test",
	};
	
	$.ajax ({
		url: jsonurl,
		dataType: "json",
		async: false,
		success: function(responseData) {
		}
	})
	
	//source.clear();

	map.updateSize();
}

// 중심 좌표와 줌 레벨 input의 값으로 지도 이동
function moveToCenterAndZoom() {
    // 입력된 중심 좌표를 가져와 경도와 위도로 분리
    
    const levelInput = document.getElementById('level');
    const coordInput = document.getElementById('coord');
    
    const centerValue = coordInput.value.split(',');
    const x = parseFloat(centerValue[0]);
    const y = parseFloat(centerValue[1]);
    const zoom = parseFloat(levelInput.value);

    // 유효성 검사 후 지도 이동
    if (!isNaN(x) && !isNaN(y)) {
        
        view.setCenter([x,y]); // 중심 좌표 설정
    }
    if (!isNaN(zoom)) {
        view.setZoom(zoom-1); // 줌 레벨 설정
    }
}


function logCenterAndZoom() {
    const center = view.getCenter(); 
    const zoom = view.getZoom();
    console.log('중심 좌표:', center);
    console.log('줌 레벨:', zoom);
    
    const levelInput = document.getElementById('level');
    const coordInput = document.getElementById('coord');
    
    coordInput.value = `${center[0].toFixed(0)},${center[1].toFixed(0)}`;
    levelInput.value = (zoom+1).toFixed(0);
    
    const extent = view.calculateExtent(map.getSize()); // 현재 화면의 Extent
    mbr.value = extent.map(coord => coord.toFixed(0)).join(','); // 정수로 표시
    
    
}