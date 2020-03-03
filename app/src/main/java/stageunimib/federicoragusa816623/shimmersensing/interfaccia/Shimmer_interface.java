package stageunimib.federicoragusa816623.shimmersensing.interfaccia;

public interface Shimmer_interface {
    String IP_SERVER = "192.168.1.16";
    String PORT_SERVER = ":5000";
    String URL_FILE = "http://"+IP_SERVER+PORT_SERVER;
    String URL_SERVER = URL_FILE+"/api/v1/resources/shimmersensing/sensordata/";
    boolean DEBUG_SHIMMER = true;

}
