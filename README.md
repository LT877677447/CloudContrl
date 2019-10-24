# 云控系统简述 updateTime:2019/10/22 
## 1.什么是云控系统？    
    简述：以几十、上百、成千台手机（客户端）与一台或者多台服务器通信，通过http协议，服务端对手机客户端进行任务调度，手机客户端模拟用户行为，达到制造流量数据的目的。

客户端要做的：确定要进行刷流量的app后，找到该app的apk包，反编译android代码后，分析代码中的api调用和业务处理，并在适当的地方加入自己的代码，(有时涉及通信，还需要深入协议层面),完成脚本的制作，

服务端要做的：把要做的任务计划添加到数据库中，在服务端任务线程每天初始化时，从数据库扫描初始化出每天要做的各种类型的任务，并在客户端请求时，把任务信息下发给手机客户端，手机端根据任务信息，进行特定行为，并在每次完成任务后，向服务端发回任务执行结果，服务端处理结果报告，记录日志，并更新任务运行时状态。

## 例如：今天商务接到一个普通包cpa的单子，是要给一个手游app刷注册量。  
### 客户端：  

    负责安卓逆向的工程师拿到该手游app的apk包，反编译出Android源码，分析api调用，根据要模拟的用户行为写出脚本。（包括安装app、获取手机号完成注册，登录、创建角色并完成游戏内的新手任务、等等）  
### 服务端：  

>1. 根据客户的定制需求，设置任务的执行参数并添加到数据库相关的任务表中。  
>2. 系统的任务刷新线程每天零点刷新，根据任务相关表数据，更新每日计划要做的任务。  
>3. 接收手机客户端的请求，从设备信息service拿一个随机的Android手机设备信息，并根据设备编号和任务表设定，从任务分发service中提取对应任务下发。  
>4. 手机拿到设备信息json、任务json，请求文件服务器下载相应apk包和脚本文件，开始做任务。    
>5. 手机注册需要手机号和，于是请求服务端，服务端去第三方平台http获取手机号给客户端，随后客户端需要验证码，同样由服务端从接码平台接口拿回来。   
>6. 客户端完成脚本指令（登录，注册，玩游戏等等），向服务端发回任务执行结果报告，服务端处理报告，数据库记录日志，根据任务成功失败，更新任务集合信息和任务表数据。  

### 任务的执行参数：  
时间范围 7天 ，每日新增 500 ， 留存曲线：70% 45% 20% 0%  ，新增时间曲线：[0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,0,0] ，新增时间：25分钟， 留存时间 20分钟 ，是否锁定机器：[] ， vpn渠道：...    

### 参数说明:    
* 时间范围：刷量的时间范围，超过后新增和留存的量都不在生效    
* 每日新增：每天新的注册用户量。  
* 留存曲线：新账号注册后，第二天开始就算留存了，70%表示第二天留存率为70%,也就是第一天新注册的500用户中随机的70%用户再次上线在app上活跃，45%就是到了         第三天只剩第一天新注册的500用户的45%需要在app上活跃。  
* 新增时间曲线：每天做新增用户时，在当天的24小时内的，每个小时应该分配的新增用户占比。  
* 新增时间：做新增注册时，手机客户端脚本整个的执行最大时间限制，必须在此时间范围内完成注册、创建角色、试玩游戏。  
* 是否锁定机器：因为app的服务器有时检测手机机器信息，所以可以设置锁定某台手机设备只从服务器取某一个app的任务来做。  

### 整体来说，云控系统包含服务端任务调度系统和客户端逆向app工作，主要难点还是在客户端的逆向app工程。

