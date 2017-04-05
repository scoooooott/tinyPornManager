// package org.tinymediamanager.thirdparty;
//
// import java.net.InetAddress;
// import java.net.URI;
// import java.net.URISyntaxException;
// import java.net.UnknownHostException;
// import java.nio.file.InvalidPathException;
// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.util.Arrays;
// import java.util.List;
//
// import org.jsoup.helper.StringUtil;
// import org.junit.AfterClass;
// import org.junit.BeforeClass;
// import org.junit.Test;
// import org.tinymediamanager.core.Settings;
// import org.tinymediamanager.jsonrpc.api.AbstractCall;
// import org.tinymediamanager.jsonrpc.api.call.Files;
// import org.tinymediamanager.jsonrpc.api.call.VideoLibrary;
// import org.tinymediamanager.jsonrpc.api.model.FilesModel;
// import org.tinymediamanager.jsonrpc.api.model.ListModel;
// import org.tinymediamanager.jsonrpc.api.model.VideoModel;
// import org.tinymediamanager.jsonrpc.api.model.VideoModel.MovieDetail;
// import org.tinymediamanager.jsonrpc.api.model.VideoModel.TVShowDetail;
// import org.tinymediamanager.jsonrpc.config.HostConfig;
// import org.tinymediamanager.jsonrpc.io.ApiCallback;
// import org.tinymediamanager.jsonrpc.io.ConnectionListener;
// import org.tinymediamanager.jsonrpc.io.JavaConnectionManager;
// import org.tinymediamanager.jsonrpc.notification.AbstractEvent;
//
// public class JsonRpc {
//
// // *************************************************************************************
// // you need to enable Kodi -> remote control from OTHER machines (to open TCP port 9090)
// // *************************************************************************************
//
// private static JavaConnectionManager cm = new JavaConnectionManager();
//
// @Test
// public void events() {
// while (true) {
// // do nothing, just wait for events...
// }
// }
//
// @Test
// public void pe() {
// // absolute=true, else false
// Path p1 = Paths.get("/storage/movies").toAbsolutePath();
// Path p2 = Paths.get("c:\\storage\\movies").toAbsolutePath();
// System.out.println(p1.equals(p2));
// System.out.println(p1.toFile().equals(p2.toFile()));
// }
//
// @Test
// public void getDataSources() throws InterruptedException {
// Settings.getInstance();
// final Files.GetSources f = new Files.GetSources(FilesModel.Media.VIDEO); // movies + tv !!!
// cm.call(f, new ApiCallback<ListModel.SourceItem>() {
//
// @Override
// public void onResponse(AbstractCall<ListModel.SourceItem> call) {
// System.out.println(" found " + call.getResults().size() + " sources");
//
// System.out.println("\n--- KODI DATASOURCES ---");
// for (ListModel.SourceItem res : call.getResults()) {
// System.out.println(res.file + " - " + Arrays.toString(getIpAndPath(res.file)));
// }
//
// System.out.println("\n--- TMM DATASOURCES ---");
// for (String ds : Settings.getInstance().getMovieSettings().getMovieDataSource()) {
// System.out.println(ds + " - " + Arrays.toString(getIpAndPath(ds)));
// }
// for (String ds : Settings.getInstance().getTvShowSettings().getTvShowDataSource()) {
// System.out.println(ds + " - " + Arrays.toString(getIpAndPath(ds)));
// }
//
// String ds = "//server/asdf";
// System.out.println(ds + " - " + Arrays.toString(getIpAndPath(ds)));
//
// }
//
// @Override
// public void onError(int code, String message, String hint) {
// System.out.println("Error " + code + ": " + message);
// }
// });
//
// Thread.sleep(20 * 1000);
// }
//
// /**
// * gets the resolved IP address of UNC/SMB path<br>
// * \\hostname\asdf or smb://hostname/asdf will return the IP:asdf
// *
// * @param ds
// * TMM/Kodi datasource
// * @return IP:path, or LOCAL:path
// */
// private String[] getIpAndPath(String ds) {
// String[] ret = { "", "" };
// URI u = null;
// try {
// u = new URI(ds);
// }
// catch (URISyntaxException e) {
// try {
// Path p = Paths.get(ds).toAbsolutePath();
// u = p.toUri();
// }
// catch (InvalidPathException e2) {
// e.printStackTrace();
// }
// }
// if (!StringUtil.isBlank(u.getHost())) {
// ret[1] = u.getPath();
// if (ds.startsWith("upnp")) {
// ret[0] = getMacFromUpnpUUID(u.getHost());
// }
// else {
// try {
// InetAddress i = InetAddress.getByName(u.getHost());
// ret[0] = i.getHostAddress().toString();
// }
// catch (UnknownHostException e) {
// ret[0] = u.getHost();
// }
// }
// }
// else {
// ret[0] = "LOCAL";
// ret[1] = u.getPath();
// }
// return ret;
// }
//
// /**
// * gets the MAC from an upnp UUID string (= last 6 bytes reversed)<br>
// * like upnp://00113201-aac2-0011-c2aa-02aa01321100 -> 00113201AA02
// *
// * @param uuid
// * @return
// */
// private String getMacFromUpnpUUID(String uuid) {
// String s = uuid.substring(uuid.lastIndexOf('-') + 1);
// StringBuilder result = new StringBuilder();
// for (int i = s.length() - 2; i >= 0; i = i - 2) {
// result.append(new StringBuilder(s.substring(i, i + 2)));
// }
// return result.toString().toUpperCase();
// }
//
// @Test
// public void getAllMovies() {
// final VideoLibrary.GetMovies vl = new VideoLibrary.GetMovies();
// cm.call(vl, new ApiCallback<VideoModel.MovieDetail>() {
//
// @Override
// public void onResponse(AbstractCall<MovieDetail> call) {
// System.out.println(" found " + call.getResults().size() + " movies");
// for (MovieDetail res : call.getResults()) {
// System.out.println(" " + res);
// }
// }
//
// @Override
// public void onError(int code, String message, String hint) {
// System.out.println("Error " + code + ": " + message);
// }
// });
// }
//
// @Test
// public void getAllTvShows() {
// final VideoLibrary.GetTVShows vl = new VideoLibrary.GetTVShows();
// cm.call(vl, new ApiCallback<VideoModel.TVShowDetail>() {
//
// @Override
// public void onResponse(AbstractCall<TVShowDetail> call) {
// System.out.println(" found " + call.getResults().size() + " shows");
// for (TVShowDetail res : call.getResults()) {
// System.out.println(" " + res);
// }
// }
//
// @Override
// public void onError(int code, String message, String hint) {
// System.out.println("Error " + code + ": " + message);
// }
// });
// }
//
// @BeforeClass
// public static void setUp() {
// HostConfig config = new HostConfig(detectKodi(), 80, 9090);
// cm.registerConnectionListener(new ConnectionListener() {
//
// @Override
// public void notificationReceived(AbstractEvent event) {
// // System.out.println("Event received: " + event.getClass().getCanonicalName());
// System.out.println("Event received: " + event);
// }
//
// @Override
// public void disconnected() {
// System.out.println("Event: Disconnected");
//
// }
//
// @Override
// public void connected() {
// System.out.println("Event: Connected");
//
// }
// });
// System.out.println("Connecting...");
// cm.connect(config);
// }
//
// @AfterClass
// public static void tearDown() throws InterruptedException {
// Thread.sleep(1000); // wait a bit - async
// System.out.println("Exiting...");
// cm.disconnect();
// }
// }
