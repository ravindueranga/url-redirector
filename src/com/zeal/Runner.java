package com.zeal;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

class Runner {

    static final String FOLDER_NAME = "files";
    static final String REDIRECTED_LIST = "redirected lists";
    static final int RECORD_THRESHOLD = 250;

    void run() throws IOException {

        createFolder(REDIRECTED_LIST);

        List<String> fileList = listFilesForFolder(FOLDER_NAME);

        for (String s : fileList) {

            int counter = 0;

            System.out.println(s);

            FileWriter fw = new FileWriter(REDIRECTED_LIST + "/" + s.substring(0, s.length() - 4) + "_redir.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);

            try {
                BufferedReader br = new BufferedReader(new FileReader(FOLDER_NAME + "/" + s));
                String str;
                while ((str = br.readLine()) != null) {
                    ++counter;
                    if (counter % RECORD_THRESHOLD == 0) {
                        System.out.println(RECORD_THRESHOLD + " urls reached");
                    }

                    str = str.replace("\uFEFF", "");

                    out.println(getRedirectedUrl(str));
                }

            } finally {
                out.close();
                System.out.println("Finished : " + s);
            }
        }

        System.out.println("Done getting redirected URLs");
    }

    /**
     * This method will get the redirected url of a given url if there is any. If not pass the url back as it is
     *
     * @param url
     * @return
     */
    private String getRedirectedUrl(String url) {

        try {
            HttpURLConnection con = createConnection(url);
            con.connect();

            if (con.getResponseCode() != 200) {

                String location = con.getHeaderField("Location");
                if (location == null || !location.contains("http")) {
                    return url;
                } else {
                    return location;
                }

            } else {
                return url;
            }
        } catch (Exception e) {
            return url;
        }
    }

    private HttpURLConnection createConnection(String str) {

        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) (new URL(str).openConnection());
        } catch (IOException e) {
            e.printStackTrace();
        }
        con.setConnectTimeout(2000);
        con.setReadTimeout(2000);
        con.setInstanceFollowRedirects(false);

        return con;
    }

    private boolean createFolder(String redirectedList) {

        File f = new File(redirectedList);
        boolean isCreated = f.mkdir();

        if (isCreated) {
            System.out.println("Folder Created");
        }

        return isCreated;
    }

    /**
     * Get all the file names in a given FOLDER_NAME
     *
     * @param folderPath
     * @return
     * @throws IOException
     */
    private List<String> listFilesForFolder(final String folderPath) throws IOException {

        return Files.walk(Paths.get(folderPath)).filter(Files::isRegularFile).map(Path::toFile).map(File::getName).collect(Collectors.toList());
    }
}
