## 云控服务器


### 请尽量按照MVC规范编写代码

#### 访问顺序

* View<---->Action<--->Service<--->DAO--->DB
 

#### 类名规范

*  DAO: xxxDAO(接口类), xxxDAOImpl(实现类)
*  Service：xxxService(接口类), xxxServiceImpl(实现类)
    
#### 概念

* po, persistence object, 持久层对象: 对象的属性和数据库表的字段一一对应
* bo, business object, 业务层对象: 对象的属性和当前业务逻辑所需的数据的名称一一对应
* vo, view object, 表现层对象: 对象的属性和页面层展示的数据的名称一一对应


    另外还有
    * pojo, plain ordinary java object, 普通JAVA对象，只有属性及其set/get方法
    * dto, data transfer object，数据传输对象，用在需要跨进程或远程传输时，它不应该包含业务逻辑
