## 使用方法
**1,在project build.gradle添加**
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
}
```
**2.在Module build.gradle添加依赖**
```
dependencies {
	        compile 'com.github.limin13215:VismGethLibrary:v1.0.2'
}
```

## 调用API
封装的类: 
   VismGeth mVismGeth = new VismGeth(this);
