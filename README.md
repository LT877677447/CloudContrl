## �ƿط�����


### �뾡������MVC�淶��д����

#### ����˳��

* View<---->Action<--->Service<--->DAO--->DB
 

#### �����淶

*  DAO: xxxDAO(�ӿ���), xxxDAOImpl(ʵ����)
*  Service��xxxService(�ӿ���), xxxServiceImpl(ʵ����)
    
#### ����

* po, persistence object, �־ò����: ��������Ժ����ݿ����ֶ�һһ��Ӧ
* bo, business object, ҵ������: ��������Ժ͵�ǰҵ���߼���������ݵ�����һһ��Ӧ
* vo, view object, ���ֲ����: ��������Ժ�ҳ���չʾ�����ݵ�����һһ��Ӧ


    ���⻹��
    * pojo, plain ordinary java object, ��ͨJAVA����ֻ�����Լ���set/get����
    * dto, data transfer object�����ݴ������������Ҫ����̻�Զ�̴���ʱ������Ӧ�ð���ҵ���߼�
