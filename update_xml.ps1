$files = Get-ChildItem -Path "d:\workspace3\mapservice\map_service\*.xml"
foreach ($f in $files) {
    $content = Get-Content $f.FullName -Raw
    $content = $content -replace '<TileWidth>256</TileWidth>', '<TileWidth>512</TileWidth>'
    $content = $content -replace '<TileHeight>256</TileHeight>', '<TileHeight>512</TileHeight>'
    Set-Content -Path $f.FullName -Value $content -Encoding UTF8
}
