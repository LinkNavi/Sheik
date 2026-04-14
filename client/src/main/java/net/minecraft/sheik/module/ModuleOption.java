package net.minecraft.sheik.module;

public class ModuleOption<T> {
private String name;
private T value;
private T defaultValue;
private Class<T> type;

public ModuleOption(String name, T value, T defaultValue, Class<T> type) {
    this.name = name;
    this.value = value;
    this.defaultValue = defaultValue;
    this.type = type;
}

public String getName() {
    return name;
}

public void setValueFromString(String str) {
    if (type == Boolean.class) {
        value = type.cast(Boolean.parseBoolean(str));
    } else if (type == Integer.class) {
        value = type.cast(Integer.parseInt(str));
    } else if (type == Float.class) {
        value = type.cast(Float.parseFloat(str));
    } else if (type == String.class) {
        value = type.cast(str);
    }
}

public T getValue() {
    return value;
}

public T getDefaultVaule(){
    return defaultValue;
}

public void setValue(T value) {
    this.value = value;
}

}