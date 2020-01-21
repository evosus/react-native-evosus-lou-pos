package com.evosus.loupos;

import android.text.TextUtils;

import com.pax.poslink.CommSetting;
import com.pax.poslink.LogSetting;
//import com.pax.poslink.internal.Convenience;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by linhb on 2015-09-05.
 */
public class SettingINI {
    public final static String FILENAME = "setting.ini";


    private final static String Deft = "";
    private final static String DeftTimeout = "30000";

    private final static String SectionComm = "COMMUNICATE";
    private final static String TagComm = "CommType";
    private final static String TagIp = "IP";
    private final static String TagPortnum = "SERIALPORT";
    private final static String TagBaudrate = "BAUDRATE";
    private final static String TagPort = "PORT";
    private final static String TagTimeout = "TIMEOUT_M";
    private final static String TagMacAddr = "MACADDR";
    private final static String TagLastSN = "LASTSN";
    private final static String TagLastTermId = "LASTTERMID";
    private final static String SectionLog = "LOG";
    private final static String TagMode = "MODE";
    private final static String TagLevel = "LEVEL";
    private final static String TagOutputPath = "OUTPUTFILE";
    private final static String TagHost = "HOST";
    private final static String TAG_ENABLE_PROXY = "ENABLE_PROXY";

    public static boolean saveCommSettingToFile(final String fileName, final CommSetting commsetting)
    {
        IniFile ini;
        ini = new IniFile(fileName);
        ini.setSection(SectionComm);

        boolean bDone = ini.write(TagComm, commsetting.getType());
        bDone &= ini.write(TagTimeout, commsetting.getTimeOut());
        bDone &= ini.write(TagPortnum, commsetting.getSerialPort());
        bDone &= ini.write(TagBaudrate, commsetting.getBaudRate());
        bDone &= ini.write(TagIp, commsetting.getDestIP());
        bDone &= ini.write(TagPort, commsetting.getDestPort());
        bDone &= ini.write(TagMacAddr, commsetting.getMacAddr());
//        bDone &= ini.write(TagHost, Convenience.getHost(MainApplication.getInstance().getApplicationContext(), commsetting));
        bDone &= ini.write(TAG_ENABLE_PROXY, String.valueOf(commsetting.isEnableProxy()));
        return bDone;
    }

    public static CommSetting getCommSettingFromFile(final String fileName)
    {
        IniFile ini;
        ini = new IniFile(fileName);
        ini.setSection(SectionComm);

        CommSetting commsetting = new CommSetting();
        commsetting.setTimeOut(ini.read(TagTimeout, DeftTimeout));
        commsetting.setType(ini.read(TagComm, Deft));
        commsetting.setSerialPort(ini.read(TagPortnum, Deft));
        commsetting.setBaudRate(ini.read(TagBaudrate, Deft));
        commsetting.setDestIP(ini.read(TagIp, Deft));
        commsetting.setDestPort(ini.read(TagPort, Deft));
        commsetting.setMacAddr(ini.read(TagMacAddr, Deft));
//        Convenience.setHost(MainApplication.getInstance().getApplicationContext(), commsetting, ini.read(TagHost, Deft));
        String enableProxy = ini.read(TAG_ENABLE_PROXY, Deft);
        if (!TextUtils.isEmpty(enableProxy)) {
            commsetting.setEnableProxy(Boolean.parseBoolean(enableProxy));
        }
        return commsetting;
    }

    public static boolean saveLastSN(final String fileName, final String SN)
    {
        IniFile ini;
        ini = new IniFile(fileName);
        ini.setSection(SectionComm);

        return ini.write(TagLastSN, SN);
    }

    public static String getLastSN(final String fileName)
    {
        IniFile ini;
        ini = new IniFile(fileName);
        ini.setSection(SectionComm);

        return ini.read(TagLastSN, Deft);
    }

    public static boolean saveLastTermId(final String fileName, final String termId)
    {
        IniFile ini;
        ini = new IniFile(fileName);
        ini.setSection(SectionComm);

        return ini.write(TagLastTermId, termId);
    }

    public static String getLastTermId(final String fileName)
    {
        IniFile ini;
        ini = new IniFile(fileName);
        ini.setSection(SectionComm);

        return ini.read(TagLastTermId, Deft);
    }

    public static boolean saveLogSettingToFile(final String fileName)
    {
        IniFile ini;
        ini = new IniFile(fileName);
        ini.setSection(SectionLog);

        boolean bDone = ini.write(TagMode, Boolean.toString(LogSetting.isLoggable()));
        bDone &= ini.write(TagLevel, LogSetting.getLevel().toString());
        bDone &= ini.write(TagOutputPath, LogSetting.getOutputPath());
        return bDone;
    }

    public static boolean loadSettingFromFile(final String fileName)
    {
        IniFile ini;
        ini = new IniFile(fileName);
        ini.setSection(SectionLog);
        boolean bDone = false;
        String temp = ini.read(TagMode, Deft);
        if(temp.length() > 0)
        {
            bDone = LogSetting.setLogMode(Boolean.parseBoolean(temp));
        }

        if(bDone) {
            temp = ini.read(TagLevel, Deft);
            if (temp.length() > 0) {
                bDone = LogSetting.setLevel(LogSetting.LOGLEVEL.valueOf(temp));
            }
        }

        if(bDone)
        {
            temp = ini.read(TagOutputPath, Deft);
            if(temp.length() > 0)
            {
                bDone = LogSetting.setOutputPath(temp);
            }
        }

        return bDone;
    }
}

class IniFile {

    public static final int MAX_INI_FILE_SIZE  = 1024*16;

    private String m_fileName;
    private String m_section;


    public IniFile(final String fileName)
    {
        m_fileName=fileName;

        File fconfig = new File(m_fileName);
        if (fconfig.exists())
        {
            //System.out.println("file is exist!");
            try{
                String command = "chmod 666 " + m_fileName;
                Runtime runtime = Runtime.getRuntime();
                runtime.exec(command);
            }catch(IOException e)
            {
                System.out.println("chmod 666 failed!");
            }
        }
        else
        {
            try {
                if (fconfig.createNewFile())
                {
                    //System.out.println("create successful!");
                    try{
                        String command = "chmod 666 " + m_fileName;
                        Runtime runtime = Runtime.getRuntime();
                        runtime.exec(command);
                    }catch(IOException e)
                    {
                        System.out.println("chmod 666 failed!");
                    }
                }
            }
            catch (IOException e)
            {
                //e.printStackTrace();
            }
        }
    }

    public final String getFileName()
    {
        return m_fileName;
    }

    public final String getSection()
    {
        return m_section;
    }
    public void setSection(final String section)
    {
        m_section = section;
    }

    public boolean write(final String key, final String value)
    {
        return (write_profile_string(m_section,key,value,m_fileName)==1);
    }

    public boolean write(final String key, int value)
    {
        StringBuffer tmp = new StringBuffer(64);
        tmp.delete(0, tmp.capacity());
        tmp.append(value);
        return write(key, tmp.toString());
    }

    public String read(final String key, final String default_value)
    {
        StringBuffer buf=new StringBuffer(4096);
        read_profile_string(m_section,key,buf,buf.capacity(),default_value,m_fileName);
        return buf.toString();
    }
    public int read(final String key, int default_value)
    {
        return read_profile_int(m_section,key,default_value,m_fileName);
    }

    private static int load_ini_file(final String file, StringBuffer buf, int file_size[])
    {

        try {
            File fconfig = new File(file);
            if (!fconfig.exists())
            {
                //System.out.println("file is not exist!");
                return 0;
            }
            FileReader in=new FileReader(file);
            file_size[0] =0;

            char data[]=new char[MAX_INI_FILE_SIZE];

            int num = in.read(data);
            if(num>0)
            {
                String str=new String(data,0,num);
                buf.delete(0, buf.capacity());
                buf.append(str);
                file_size[0]=num;
            }
            in.close();

        } catch (IOException e) {
            //e.printStackTrace();
            return 0;
        }

        return 1;
    }
    private static int newline(char c)
    {
        return ('\n' == c ||  '\r' == c )? 1 : 0;
    }
    /*//java not supported endchar
    private static int end_of_string(char c)
    {
        return '\0'==c? 1 : 0;
    }
    */
    private static int left_barce(char c)
    {
        return '[' == c? 1 : 0;
    }
    private static int right_brace(char c )
    {
        return ']' == c? 1 : 0;
    }
    private static int parse_file(final String section, final String key, final String buf, int sec_s[], int sec_e[],
                                  int key_s[], int key_e[], int value_s[], int value_e[])
    {
        final String p = buf;
        int i=0;

        sec_s[0]=sec_e[0] = key_e[0] = key_s[0] = value_s[0] = value_e[0] = -1;

        while(i<p.length()){
            //find the section

            if(( 0==i || newline(p.charAt(i-1))==1) && left_barce(p.charAt(i))==1)
            {
                int section_start=i+1;

                //find the ']'
                do {
                    i++;
                } while( right_brace(p.charAt(i))==0 && i<p.length());

                //System.out.println("section_start  " + section_start);
                //System.out.println("i-section_start  " + (i-section_start));
                //System.out.println("write section is   " + section);
                //System.out.println("file section is   " + p.substring(section_start,i-section_start));
                //System.out.println("file content is   " + p);
                if(section.equals(p.substring(section_start,i))) {
                    int newline_start=0;

                    i++;

                    //Skip over space char after ']'
                    while(p.charAt(i)==' ') {
                        i++;
                    }

                    //find the section
                    sec_s[0] = section_start;
                    sec_e[0] = i;

                    //System.out.println("sec_s[0] is " + sec_s[0]);
                    //System.out.println("sec_e[0] is " + sec_e[0]);
                    while( i<p.length()&&(newline(p.charAt(i-1))==0 || left_barce(p.charAt(i))==0) )
                    {
                        //System.out.println("j char is   " + p.charAt(j));
                        //get a new line
                        newline_start = i;

                        while( newline(p.charAt(i))==0 &&  i<p.length() ) {
                            i++;
                        }

                        //now i  is equal to end of the line
                        int j = newline_start;

                        if(';' != p.charAt(j)) //skip over comment
                        {
                            while(j < i && p.charAt(j)!='=') {
                                //System.out.println("j char is   " + p.charAt(j));
                                j++;
                                //System.out.println("j+1 char is " + p.charAt(j));
                                if('=' == p.charAt(j)) {
                                    //System.out.println("newline_start  " + newline_start);
                                    //System.out.println("j is   " + j);
                                    //System.out.println("key is   " + key);
                                    //System.out.println("file key is   " + p.substring(newline_start,j));
                                    //System.out.println("file content is   " + p);

                                    if(key.equals(p.substring(newline_start,j)))
                                    {
                                        //find the key ok
                                        //System.out.println("not find the key ");
                                        key_s[0] = newline_start;
                                        key_e[0] = j-1;

                                        value_s[0] = j+1;
                                        value_e[0] = i;
                                        //System.out.println("the key_s is  "+key_s[0]);
                                        return 1;
                                    }
                                }
                            }
                        }

                        i++;
                    }
                }
            }
            else
            {
                i++;
            }
        }
        return 0;
    }

    public static int read_profile_string(final String section, final String key, StringBuffer value,
                                          int size, final String default_value, final String file)
    {
        StringBuffer buf=new StringBuffer(MAX_INI_FILE_SIZE);

        int file_size[]=new int[1];
        int sec_s[]=new int[1];
        int sec_e[]=new int[1];
        int key_s[]=new int[1];
        int key_e[]=new int[1];
        int value_s[]=new int[1];
        int value_e[]=new int[1];

        file_size[0]=sec_s[0]=sec_e[0]=key_s[0]=key_e[0]=value_s[0]=value_e[0]=0;
        //check parameters


        if(load_ini_file(file,buf,file_size)==0)
        {
            if(default_value!=null)
            {
                value.delete(0, value.length());
                value.append(default_value);
            }
            return 0;
        }

        if(parse_file(section,key,buf.toString(),sec_s,sec_e,key_s,key_e,value_s,value_e)==0)
        {
            if(default_value!=null)
            {
                value.delete(0, value.length());
                value.append(default_value);
            }
            return 0; //not find the key
        }
        else
        {
            int cpcount = value_e[0] -value_s[0];

            if( size-1 < cpcount)
            {
                cpcount =  size-1;
            }

            value.delete(0, value.length());
            value.append(buf.toString().substring(value_s[0], value_s[0]+cpcount));

            return 1;
        }
    }
    public static int read_profile_int(final String section, final String key, int default_value,
                                       final String file)
    {
        StringBuffer value =new StringBuffer(32);

        if(read_profile_string(section,key,value, value.capacity(),null,file)==0)
        {
            return default_value;
        }
        else
        {
            return Integer.parseInt(value.toString());
        }
    }

    /**
     * write a profile string to a ini file
     * @param section [in] name of the section,can't be NULL and empty string
     * @param key [in] name of the key pairs to value, can't be NULL and empty string
     * @param value [in] profile string value
     * @param file [in] path of ini file
     * @return 1 : success\n 0 : failure
     */
    public static int write_profile_string(final String section, final String key,
                                           final String value, final String file)
    {
        StringBuffer buf=new StringBuffer(MAX_INI_FILE_SIZE);
        StringBuffer w_buf=new StringBuffer(MAX_INI_FILE_SIZE);
        int file_size[]=new int[1];
        int sec_s[]= new int[1];
        int sec_e[]= new int[1];
        int key_s[]= new int[1];
        int key_e[]= new int[1];
        int value_s[]= new int[1];
        int value_e[]= new int[1];
        file_size[0]=sec_s[0]=sec_e[0]=key_s[0]=key_e[0]=value_s[0]=value_e[0]=0;


        //check parameters

        if(load_ini_file(file,buf,file_size)==0)
        {
            sec_s[0] = -1;
        }
        else
        {
            //System.out.println("file content is "+buf.toString());
            parse_file(section,key,buf.toString(),sec_s,sec_e,key_s,key_e,value_s,value_e);
        }
        //System.out.println("sec_s[0] is "+sec_s[0]);
        //System.out.println("key_s[0] is "+key_s[0]);
        if( -1 == sec_s[0])
        {

            if(0==file_size[0])
            {
                //sprintf(w_buf+file_size,"[%s]\n%s=%s\n",section,key,value);
                w_buf.insert(file_size[0], "["+section+"]"+"\n"+key+"="+value+"\n");

            }
            else
            {
                //not find the section, then add the new section at end of the file
                w_buf.delete(0, w_buf.capacity());
                w_buf.append(buf.toString().substring(0,file_size[0]));
                w_buf.insert(file_size[0], "\n"+"["+section+"]"+"\n"+key+"="+value+"\n");
            }
        }
        else if(-1 == key_s[0])
        {
            //not find the key, then add the new key=value at end of the section

            w_buf.delete(0, w_buf.capacity());
            w_buf.append(buf.toString().substring(0,sec_e[0]+1));
            w_buf.append(key+"="+value+"\n");
            w_buf.append(buf.toString().substring(sec_e[0]+1));
        }
        else
        {
            //update value with new value
            w_buf.delete(0, w_buf.capacity());

            //System.out.println("djk buf is "+buf.toString());
            //System.out.println("value_s[0] is "+value_s[0]);
            //System.out.println("djk buf key is "+buf.toString().substring(0, value_s[0]));
            //System.out.println("value_len is "+value_len);
            //System.out.println("value_e[0] is "+value_e[0]);

            w_buf.append(buf.toString().substring(0, value_s[0]));

            //System.out.println("value is "+value);
            w_buf.append(value);
            //System.out.println("file_size[0] is "+file_size[0]);
            if(value_e[0]<file_size[0])
            {
                w_buf.append(buf.toString().substring(value_e[0]));
            }

        }

        try {
            FileWriter out = new FileWriter(file);

            //System.out.println("write file content is "+w_buf.toString());
            out.write(w_buf.toString());
            out.flush();
            out.close();
        } catch (Exception e) {
            //e.printStackTrace();
            return 0;
            //e.printStackTrace();
        }

        return 1;
    }
}
