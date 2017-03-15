# SpinningWheelAndroid

![1]

## Attributes
* wheel_colors        -> Color value
* wheel_stroke_color  -> Color value
* wheel_stroke_width  -> Dimension value
* wheel_items         -> Array of string
* wheel_text_size     -> Dimension value
* wheel_text_color    -> Color value
* wheel_arrow_color   -> Color value

## Example
1) Custom view in xml
```xml
<com.teslacode.spinningwheel.SpinningWheelView
        android:id="@+id/wheel"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_weight="1"
        app:wheel_arrow_color="@android:color/black"
        app:wheel_colors="@array/rainbow_dash"
        app:wheel_items="@array/dummy"
        app:wheel_stroke_color="@android:color/black"
        app:wheel_stroke_width="5dp"/>
```

[1]: ./example/1.gif
