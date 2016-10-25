# SlideUnlock #
一个滑动解锁控件  

![SlideUnlock](/SlideUnlock.gif)

### [Apk下载](/SLideUnlock.apk) ###

## 添加依赖 ##

### Step 1. Add the JitPack repository to your build file ###
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}  

### Step 2. Add the dependency ###

	dependencies {
	        compile 'com.github.EthanCo:SlideUnlock:1.0.2'
	}

## 相关属性 ##

	 <!--普通状态下的图片-->
    <attr name="normalKeyholeSrc" format="reference" />
    <!--按下状态时候的图片-->
    <attr name="pressKeyholeSrc" format="reference" />
    <!--可以解锁/已解锁状态下的图片-->
    <attr name="unlockKeyholeSrc" format="reference" />

    <!--最大可以移动到的距离-->
    <attr name="farestDistance" format="dimension" />
    <!--可解锁时的距离 需小于farestDistance-->
    <attr name="preLockDistance" format="dimension" />
    <!--图片按钮半径-->
    <attr name="keyholeRadius" format="dimension" />  

## 使用 ##

	<com.ethanco.slideunlock.SlideUnlock
        android:id="@+id/slide_unlock"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:farestDistance="200dp"
        app:keyholeRadius="30dp"
        app:preLockDistance="150dp"
        app:normalKeyholeSrc="@mipmap/ic_lock_default"
        app:pressKeyholeSrc="@mipmap/ic_lock_press"
        app:unlockKeyholeSrc="@mipmap/ic_lock_unlock" />  

### 解锁解锁监听 ###

	slideUnlock.addUnlockListeners(new SlideUnlock.OnUnlockListener() {
        @Override
        public void onUnlock() {
            //do something
        }
    });