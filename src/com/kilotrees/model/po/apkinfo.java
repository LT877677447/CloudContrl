package com.kilotrees.model.po;

import com.kilotrees.util.StringUtil;

/**
 * apk信息表
 * 
 * 
 * CREATE TABLE [dbo].[tb_apkinfo]( [apkid] [int] NOT NULL, [packagename]
 * [varchar](50) NOT NULL, [apkname] [varchar](50) NOT NULL, [apkfile]
 * [varchar](50) NOT NULL, [reg_scriptfiles] [varchar](255) NOT NULL,
 * [rem_scriptfiles] [varchar](255) NOT NULL, [zipfiles] [varchar](5000) NOT
 * NULL, [regscriptparams] [varchar](300) NOT NULL, [remscriptparams]
 * [varchar](300) NOT NULL, [sdcard_dir] [varchar](50) NOT NULL,
 * [scriptfile_version] [int] NULL, CONSTRAINT [PK_tb_apkinfo] PRIMARY KEY
 * CLUSTERED ( [apkid] ASC )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF,
 * IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON
 * [PRIMARY] ) ON [PRIMARY]
 *
 * 
 */
public class apkinfo {
	public static String tablename = "tb_apkinfo";
	int apkid;
	String packagename;
	String apkname;
	// 文件名格式xxx_渠道号_版本号
	String apkfile;
	// 暂时不用，由下面的remain_rule决定
	// int remaintype;
	// 是否要时间曲线，0，通用留存曲线(在1-7点适当降一下数量)，其它表示留存曲线的id值，存在另一个表中
	// int timeline;
	// 留存规则，日活，三日，周，双周，月，格式：40;30;20;15;10;
	// 如果遇到-1,表示后面的不做了，比如40;20;-1,-1;-1表示做完周留存就不做了
	// 如果是空，则不做留存
	// String remain_rule;
	// 注册脚本文件名,现在假定只一个
	String reg_scriptfiles;
	// 留存脚本文件名;多个的话用;分开,之前对客户端了解不深，由于客户端只能使用一个脚本文件，不区分注册和留存，这个字段没有用，和reg_scriptfiles一样
	String rem_scriptfiles;
	// 需要压缩的全路径留存文件，多个文件用;分开,暂时手工输入,如果初始值为空，则不需要压缩，比如例行广告
	// 每个需要做留存的广告,开始时先用手工安装,多次打开运行,然后找出所有的程序生成的文件,填入此字段
	// 我们假定正常一个应用需要保存的是配置状态的信息文件,这些文件一开始时就会生成并在后面修改.以后生成的文件一般不是记录程序状态信息.
	String zipfiles = "";
	// 新增客户端无需解压文件名正则表达式
	String unzip_regex = "";
	// 新增脚本运行参数
	String regscriptparams;
	// 留存脚本运行参数
	String remscriptparams;
	// sdcard目录，手工检查并写入
	String sdcard_dir;

	int scriptfile_version = 1;

	public int getApkid() {
		return apkid;
	}

	public void setApkid(int apkid) {
		this.apkid = apkid;
	}

	public String getPackagename() {
		return packagename;
	}

	public void setPackagename(String packagename) {
		this.packagename = packagename;
	}

	public String getApkname() {
		return apkname;
	}

	public void setApkname(String apkname) {
		this.apkname = apkname;
	}

	public String getApkfile() {
		return apkfile;
	}

	public void setApkfile(String apkfile) {
		this.apkfile = apkfile;
	}

	public String getReg_scriptfiles() {
		return reg_scriptfiles;
	}

	public void setReg_scriptfiles(String reg_scriptfiles) {
		this.reg_scriptfiles = reg_scriptfiles;
	}

	// public String getRem_scriptfiles() {
	// return rem_scriptfiles;
	// }
	public void setRem_scriptfiles(String rem_scriptfiles) {
		this.rem_scriptfiles = rem_scriptfiles;
	}

	public String getZipfiles() {
		return zipfiles;
	}

	public void setZipfiles(String zipfiles) {
		if (zipfiles != null) {
			this.zipfiles = zipfiles;
		}
	}

	public String getUnzip_regex() {
		return unzip_regex;
	}

	public void setUnzip_regex(String unzip_regex) {
		if(StringUtil.isStringEmpty(unzip_regex)) {
			unzip_regex = "{}";
		}
		this.unzip_regex = unzip_regex;
	}

	public String getRegscriptparams() {
		return regscriptparams;
	}

	public void setRegscriptparams(String regscriptparams) {
		this.regscriptparams = regscriptparams;
	}

	public String getRemscriptparams() {
		return remscriptparams;
	}

	public void setRemscriptparams(String remscriptparams) {
		this.remscriptparams = remscriptparams;
	}

	public String getSdcard_dir() {
		return sdcard_dir;
	}

	public void setSdcard_dir(String sdcard_dir) {
		this.sdcard_dir = sdcard_dir;
	}

	public int getScriptfile_version() {
		return scriptfile_version;
	}

	public void setScriptfile_version(int scriptfile_version) {
		if (scriptfile_version <= 0)
			scriptfile_version = 1;
		this.scriptfile_version = scriptfile_version;
	}

	public String toString() {
		String s = "apkid=" + apkid + "\r\n";
		s += "packagename=" + packagename + "\r\n";
		s += "apkname=" + apkname + "\r\n";
		s += "apkfile=" + apkfile + "\r\n";
		// s += "remaintype=" + remaintype + "\r\n";
		// s += "timeline=" + timeline + "\r\n";
		// s += "remain_rule=" + remain_rule + "\r\n";
		s += "reg_scriptfiles=" + reg_scriptfiles + "\r\n";
		s += "rem_scriptfiles=" + rem_scriptfiles + "\r\n";
		return s;
	}
}
