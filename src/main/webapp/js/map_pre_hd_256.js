//var server = "./MapService";
//var server = "http://192.168.100.209:8070/map/MapService";
var server = "./ver1/mapservice";

var nowTileset;
var nowTile;
var areaSearchInteraction;
var areaSearchLayer;
var areaSearchFeature;

var key = "981edekdi1298d12ddk121";
var resolutions = [2088.96, 1044.48, 522.24, 261.12, 130.56, 65.28, 32.64, 16.32, 8.16, 4.08, 2.04, 1.02, 0.51, 0.255];
//var resolutions = 	[1044.48,522.24,261.12,130.56,	65.28,	32.64,	16.32,	8.16,4.08,2.04,1.02,0.51,0.255,0.1275];
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
			maxResolution: 1044.48,
			minResolution: 0.1275,
			projection: 'EPSG:5179',
			constrainResolution: true,
			zoom: 0
		}),
	
	});
	
	view = map.getView();
    view.on('change:center', logCenterAndZoom);
	view.on('change:resolution', logCenterAndZoom);
	logCenterAndZoom();
	initAreaSearch();
}


// 영역을 드래그해서 MBR을 만들고 layerlist를 조회한다.
function initAreaSearch(){
	var resultPanel = document.createElement('div');
	resultPanel.id = 'layer-list-result';
	resultPanel.style.cssText = 'display:none; position:absolute; z-index:10; top:18px; left:50%; transform:translateX(-50%); width:min(720px, calc(100% - 36px)); max-height:calc(100% - 36px); overflow:auto; padding:16px; box-sizing:border-box; background:rgba(255,255,255,.96); border:1px solid #8b98a8; border-radius:6px; box-shadow:0 4px 18px rgba(0,0,0,.22); font:14px/1.45 Arial, sans-serif; color:#243447;';
	document.querySelector('.right-pane').appendChild(resultPanel);
	restoreLayerResultPosition(resultPanel);
	var resultStyle = document.createElement('style');
	resultStyle.textContent = '#layer-list-result table{border:1px solid #c8c8c8;}#layer-list-result th,#layer-list-result td{border:1px solid #c8c8c8;padding:7px 8px;text-align:left;word-break:break-word;}#layer-list-result th{background:#f1f1f1;font-weight:600;}#layer-list-result .layer-result-titlebar{display:flex;align-items:center;justify-content:space-between;margin:-18px -16px 12px;padding:7px 8px 7px 12px;background:#e9e9e9;border-bottom:1px solid #c8c8c8;font-weight:600;cursor:move;user-select:none;}#layer-list-result .layer-result-close{border:0;background:transparent;color:#555;font-size:21px;line-height:20px;cursor:pointer;padding:0 3px;}';
	document.head.appendChild(resultStyle);
	map.on('singleclick', closeLayerListResult);

	areaSearchLayer = new ol.layer.Vector({
		source: new ol.source.Vector(),
		style: new ol.style.Style({
			fill: new ol.style.Fill({color: 'rgba(30, 136, 229, 0.16)'}),
			stroke: new ol.style.Stroke({color: '#1976d2', width: 2})
		})
	});
	map.addLayer(areaSearchLayer);

	areaSearchInteraction = new ol.interaction.DragBox({
		condition: ol.events.condition.primaryAction,
		style: new ol.style.Style({
			fill: new ol.style.Fill({color: 'rgba(30, 136, 229, 0.12)'}),
			stroke: new ol.style.Stroke({color: '#1565c0', width: 2, lineDash: [6, 4]})
		})
	});
	areaSearchInteraction.setActive(false);
	areaSearchInteraction.on('boxstart', function(){ areaSearchLayer.getSource().clear(); });
	areaSearchInteraction.on('boxend', function(){
		var geometry = areaSearchInteraction.getGeometry();
		var extent = geometry.getExtent();
		areaSearchLayer.getSource().clear();
		areaSearchFeature = new ol.Feature(geometry.clone());
		areaSearchLayer.getSource().addFeature(areaSearchFeature);
		var mbrValue = extent.map(function(value){ return Number(value).toFixed(2); }).join(',');
		document.getElementById('mbr').value = mbrValue;
		setAreaSearchActive(false);
		requestLayerList(mbrValue);
	});
	map.addInteraction(areaSearchInteraction);
	document.addEventListener('keydown', function(event){
		if (event.ctrlKey && event.shiftKey && event.key.toLowerCase() === 'a') {
			event.preventDefault();
			setAreaSearchActive(true);
		} else if (event.key === 'Escape') {
			event.preventDefault();
			cancelAreaSearch();
		}
	});
}

function setAreaSearchActive(active){
	if (!areaSearchInteraction) return;
	areaSearchInteraction.setActive(active);
	var button = document.getElementById('area-search-button');
	if (button) {
		button.textContent = active ? '영역 선택 중... (지도에서 드래그)' : '영역검색';
		button.style.backgroundColor = active ? '#1565c0' : '';
		button.style.color = active ? '#fff' : '';
	}
}

function toggleAreaSearch(){
	setAreaSearchActive(!(areaSearchInteraction && areaSearchInteraction.getActive()));
}

function cancelAreaSearch(){
	setAreaSearchActive(false);
	if (areaSearchLayer) areaSearchLayer.getSource().clear();
	areaSearchFeature = null;
	closeLayerListResult();
}

function closeLayerListResult(){
	var resultPanel = document.getElementById('layer-list-result');
	if (resultPanel) resultPanel.style.display = 'none';
	if (areaSearchLayer) areaSearchLayer.getSource().clear();
	areaSearchFeature = null;
}

function restoreLayerResultPosition(resultPanel){
	try {
		var saved = JSON.parse(localStorage.getItem('layer-list-result-position'));
		if (saved && typeof saved.left === 'number' && typeof saved.top === 'number') {
			resultPanel.style.left = saved.left + 'px';
			resultPanel.style.top = saved.top + 'px';
			resultPanel.style.transform = 'none';
		}
	} catch (error) {}
}

function enableLayerResultDragging(resultPanel){
	var titleBar = resultPanel.querySelector('.layer-result-titlebar');
	if (!titleBar) return;
	titleBar.onmousedown = function(event){
		if (event.target.closest('.layer-result-close')) return;
		event.preventDefault();
		var parentRect = resultPanel.parentElement.getBoundingClientRect();
		var panelRect = resultPanel.getBoundingClientRect();
		var offsetX = event.clientX - panelRect.left;
		var offsetY = event.clientY - panelRect.top;
		resultPanel.style.transform = 'none';
		function move(moveEvent){
			var left = moveEvent.clientX - parentRect.left - offsetX;
			var top = moveEvent.clientY - parentRect.top - offsetY;
			var maxLeft = Math.max(0, parentRect.width - panelRect.width);
			var maxTop = Math.max(0, parentRect.height - panelRect.height);
			resultPanel.style.left = Math.min(maxLeft, Math.max(0, left)) + 'px';
			resultPanel.style.top = Math.min(maxTop, Math.max(0, top)) + 'px';
		}
		function stop(){
			document.removeEventListener('mousemove', move);
			document.removeEventListener('mouseup', stop);
			try { localStorage.setItem('layer-list-result-position', JSON.stringify({left: resultPanel.offsetLeft, top: resultPanel.offsetTop})); } catch (error) {}
		}
		document.addEventListener('mousemove', move);
		document.addEventListener('mouseup', stop);
	};
}

function setLayerResultContent(content){
	var resultPanel = document.getElementById('layer-list-result');
	resultPanel.innerHTML = '<button type="button" aria-label="닫기" onclick="closeLayerListResult()" style="position:absolute; top:5px; right:7px; border:0; background:transparent; color:#555; font-size:22px; line-height:22px; cursor:pointer;">×</button>' + content;
	resultPanel.style.paddingTop = '18px';
	resultPanel.innerHTML = '<div class="layer-result-titlebar"><span>레이어 목록 결과</span><button type="button" class="layer-result-close" aria-label="닫기" onclick="closeLayerListResult()">×</button></div><div class="layer-result-body">' + content + '</div>';
	resultPanel.style.paddingTop = '16px';
	enableLayerResultDragging(resultPanel);
}

function requestLayerList(mbrValue){
	var levelId = parseInt(document.getElementById('level').value, 10);
	if (isNaN(levelId)) levelId = 14;
	var resultPanel = document.getElementById('layer-list-result');
	resultPanel.style.display = 'block';
	setLayerResultContent('<div>영역 레이어 정보를 조회 중입니다...</div>');
	var url = server + '?req=layerlist&tileSet=kmp&tileName=kmp01&levelId=' + encodeURIComponent(levelId) + '&mbr=' + encodeURIComponent(mbrValue);
	$.ajax({url: url, dataType: 'json'})
		.done(function(response){ renderLayerListResultWithCopy(response, levelId); })
		.fail(function(xhr){ setLayerResultContent('<div style="color:#b71c1c;">레이어 목록 조회에 실패했습니다. (' + xhr.status + ')</div>'); });
}

function renderLayerListResult(response, requestedLevel){
	var resultPanel = document.getElementById('layer-list-result');
	if (!response || response.result !== '0000') {
		setLayerResultContent('<div style="color:#b71c1c;">레이어 목록 조회 실패: ' + ((response && response.resultDesc) || '알 수 없는 오류') + '</div>');
		return;
	}
	var layers = Array.isArray(response.layers) ? response.layers : [];
	var rows = layers.map(function(layer){
		return '<tr><td>' + escapeHtml(layer.layerName) + '</td><td>' + escapeHtml(layer.styleName) + '</td><td>' + escapeHtml(layer.parentStyleName || '') + '</td></tr>';
	}).join('');
	setLayerResultContent('<div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;"><strong>Level: ' + escapeHtml(response.levelId == null ? requestedLevel : response.levelId) + '</strong><span>레이어 개수: ' + layers.length + '</span></div><table style="width:100%; border-collapse:collapse; table-layout:fixed;"><thead><tr><th style="width:28%;">레이어 이름</th><th style="width:42%;">스타일이름</th><th>부모스타일이름</th></tr></thead><tbody>' + (rows || '<tr><td colspan="3" style="text-align:center;">레이어가 없습니다.</td></tr>') + '</tbody></table>');
}

function escapeHtml(value){
	return String(value == null ? '' : value).replace(/[&<>"']/g, function(char){
		return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[char];
	});
}

function renderLayerListResultWithCopy(response, requestedLevel){
	if (!response || response.result !== '0000') {
		setLayerResultContent('<div style="color:#b71c1c;">레이어 목록 조회 실패: ' + ((response && response.resultDesc) || '알 수 없는 오류') + '</div>');
		return;
	}
	var layers = Array.isArray(response.layers) ? response.layers : [];
	var rows = layers.map(function(layer){
		var values = [layer.layerName, layer.styleName, layer.parentStyleName];
		return '<tr>' + values.map(function(value){
			var text = value == null ? '' : String(value);
			return '<td>' + escapeHtml(text) + '</td><td style="text-align:center;white-space:nowrap;"><button type="button" style="padding:3px 7px;cursor:pointer;" data-copy-value="' + escapeHtml(text) + '" onclick="copyLayerCell(this)">복사</button></td>';
		}).join('') + '</tr>';
	}).join('');
	var emptyRow = '<tr><td colspan="6" style="text-align:center;">레이어가 없습니다.</td></tr>';
	setLayerResultContent('<div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;"><strong>Level: ' + escapeHtml(response.levelId == null ? requestedLevel : response.levelId) + '</strong><span>레이어 개수: ' + layers.length + '</span></div><table style="width:100%; border-collapse:collapse; table-layout:fixed;"><thead><tr><th style="width:23%;">레이어 이름</th><th style="width:10%;">복사</th><th style="width:27%;">스타일이름</th><th style="width:10%;">복사</th><th style="width:20%;">부모스타일이름</th><th style="width:10%;">복사</th></tr></thead><tbody>' + (rows || emptyRow) + '</tbody></table>');
}

function copyLayerCell(button){
	var value = button.getAttribute('data-copy-value') || '';
	var originalText = button.textContent;
	function showCopied(){
		button.textContent = '복사됨';
		setTimeout(function(){ button.textContent = originalText; }, 1000);
	}
	function fallbackCopy(){
		var textarea = document.createElement('textarea');
		textarea.value = value;
		textarea.style.position = 'fixed';
		textarea.style.opacity = '0';
		document.body.appendChild(textarea);
		textarea.focus();
		textarea.select();
		try {
			if (document.execCommand('copy')) showCopied();
		} finally {
			document.body.removeChild(textarea);
		}
	}
	if (navigator.clipboard && window.isSecureContext) {
		navigator.clipboard.writeText(value).then(showCopied).catch(fallbackCopy);
	} else {
		fallbackCopy();
	}
}

function constructSourceEmap(tileset, tile) {

	var source = new ol.source.XYZ({
		projection: 'EPSG:5179',
		tileGrid: new ol.tilegrid.WMTS({
			tileSize: [256, 256],
			//tileSize: [512, 512],
			origin: ol.extent.getTopLeft(epsg_5179.getExtent()),
			resolutions: resolutions
		}),
		wrapX: false,
		crossOrigin: 'anonymous',

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


	var html ='';
		html +='<button style="margin-top:5px; height:25px; width: 200px;" onclick="reloadDesign()">주제도디자인변경업데이트</button><br>';
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
			html += '<div style="margin-top:10px; padding-bottom:10px;"><button id="area-search-button" style="height:30px; width:200px; font-weight:bold;" onclick="toggleAreaSearch()">영역검색</button></div>';
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
		if (lay !== areaSearchLayer) map.removeLayer(lay);
	}


	//map 객체에 레이어 등록
	map.addLayer(geolayer);
	if (areaSearchLayer && map.getLayers().getArray().indexOf(areaSearchLayer) === -1) map.addLayer(areaSearchLayer);
	
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
