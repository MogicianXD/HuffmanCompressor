package entity;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.text.DecimalFormat;

public class MyFile
{
    private SimpleStringProperty name;
    private SimpleLongProperty length;
    private SimpleStringProperty size;
    private SimpleStringProperty path;
    public MyFile(String name, Long length, String path) {
        this.name = new SimpleStringProperty(name);
        this.length = new SimpleLongProperty(length);
        this.path = new SimpleStringProperty(path);
        DecimalFormat df = new DecimalFormat("#.00");
        double s = length;
        if(length == 0)
            this.size = new SimpleStringProperty("");
        else if(s/1024 < 1)
            this.size = new SimpleStringProperty(df.format(s) + "B");
        else if((s = s/1024) < 1024)
            this.size = new SimpleStringProperty(df.format(s) + "KB");
        else if((s = s/1024) < 1024)
            this.size = new SimpleStringProperty(df.format(s) + "MB");
        else if((s = s/1024) < 1024)
            this.size = new SimpleStringProperty(df.format(s) + "GB");
    }
    public String getName() {
        return name.get();
    }

    public String getSize()
    {
        return size.get();
    }

    public long getLength() {
        return length.get();
    }
    public String getPath()
    {
        return path.get();
    }

}

