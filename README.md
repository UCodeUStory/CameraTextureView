# CameraTextureView
通过TextureView 自定义相机

1. 相机的open 可以在TextureView 初始化阶段 也可以在onSurfaceTextureAvailable回调中open

2. 相机的预览必须在TextureView 的onSurfaceTextureAvailable中startPreview

3. 相机的配置必须要正确，尤其预览尺寸，如果不正确就会导致各种问题


<div align="center">
<img width="380" height="654" src="https://github.com/UCodeUStory/CameraTextureView/blob/master/camera1.png"/>
</div>