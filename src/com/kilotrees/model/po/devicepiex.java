package com.kilotrees.model.po;
/**
 * 大点击用到的设备型号和屏幕大小
 *

CREATE TABLE [dbo].[tb_devicepiex](
	[main_type] [varchar](50) NOT NULL,
	[type] [varchar](50) NOT NULL,
	[pixel] [varchar](50) NOT NULL
) ON [PRIMARY]

 */
public class devicepiex {
	public static String tablename= "tb_devicepiex";
	//设备主类别，比如PC,Android,Iphone
	String main_type;
	//设备牌子，比如HuaWei
	String type;
	//屏大小360x640
	String pixel;
	public String getMain_type() {
		return main_type;
	}
	public void setMain_type(String main_type) {
		this.main_type = main_type;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPixel() {
		return pixel;
	}
	public void setPixel(String pixel) {
		this.pixel = pixel;
	}
	
	
}
