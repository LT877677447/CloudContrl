package com.kilotrees.model.po;

/**
 * 机型信息表
 * 



CREATE TABLE [dbo].[tb_phonetype](
	[p_id] [int] IDENTITY(1,1) NOT NULL,
	[phone_type] [varchar](50) NOT NULL,
	[phone_info] [text] NULL,
	[use_radio] [int] NOT NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

 *
 */
public class phonetype {
	public static String tablename = "tb_phonetype";
	int p_id;//自增
	String phone_type;//机型号
	//对应机型的强匹配硬件信息，比如cpu，屏幕大小，小米手机不能用麒麟芯片，每个型号的屏大小也有不同,这里保存为一个json
	String phone_info;
	//此型号在市面上的占有率，这里用权重就行了．把最热门的机器设高些权重，然后按权重随机分发，免得每次慢慢计算百分率
	int use_radio;
	
	
	public int getP_id() {
		return p_id;
	}
	public void setP_id(int p_id) {
		this.p_id = p_id;
	}
	public String getPhone_type() {
		return phone_type;
	}
	public void setPhone_type(String phone_type) {
		this.phone_type = phone_type;
	}
	public String getPhone_info() {
		return phone_info;
	}
	public void setPhone_info(String phone_info) {
		this.phone_info = phone_info;
	}
	public int getUse_radio() {
		return use_radio;
	}
	public void setUse_radio(int use_radio) {
		this.use_radio = use_radio;
	}	
}
