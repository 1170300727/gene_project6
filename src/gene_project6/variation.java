package gene_project6;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class variation {
  
  // the reference string
  String ref;
  // the reads which have mismatches
  ArrayList<Read> reads = new ArrayList<Read>(); 
  int readLength;
  ArrayList<Vcf> vcfs = new ArrayList<Vcf>(); 
  
  public static void main(String[] args) {
    variation va = new variation();
    va.readFromFile();
    va.find();
    va.writeToFile();
  }
  
  public void writeToFile() {
    try { File writeName = new File("lib/VariationOutput.txt"); // 相对路径，如果没有则要建立一个新的output.txt文件 
    writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
    try (FileWriter writer = new FileWriter(writeName);
        BufferedWriter out = new BufferedWriter(writer)
    ) { 
      int size = vcfs.size();
      out.write("Position" + "\t");
      out.write("refBase:" + "\t");
      out.write("readBase:" + "\t");
      out.write("sumRead:"  + "\t");
      out.write("variationRead:" + "\t");
      out.write("\r\n");
      for (int i = 0;i < size;i++) {
        Vcf re = vcfs.get(i);
        out.write(re.position + "\t");
        out.write(re.refc + "\t\t");
        out.write(re.readc + "\t\t");
        out.write(re.sum  + "\t\t");
        out.write(re.mis + "\t\t");
        out.write("\r\n");
      }
    
      out.flush(); // 把缓存区内容压入文件 
      } 
    } catch (IOException e) { 
      e.printStackTrace(); 
    }
  }
  
  public boolean find() {
    
    int readsID = 0;
    int refLength = ref.length();
    System.out.println(refLength);
    int refPosition = 17050678;
    while (refPosition < 17058055 + readLength - 2) {
      Read read = reads.get(readsID);
      // find the first read concluding the position
      while (read.position - 2 + readLength < refPosition) {
        readsID++;
        read = reads.get(readsID);
        //System.out.println(refPosition);
      }
      // the read don't conclude the position actually,add the refPosition
      if (read.position - 1 > refPosition) {
        refPosition = read.position - 1;
        //System.out.println(refPosition);
        continue;
      } else {
        int sum = 0;
        int mis = 0;
        char readc = 0;
        char difreadc = 0;
        char refc = ref.charAt(refPosition);
        int tempReadsID = readsID;
        while (read.position - 1 <= refPosition) {
          
          readc = read.str.charAt(refPosition - read.position + 1);
          sum++;
          if (readc != refc) {
            difreadc = readc;
            mis++;
          }
          tempReadsID++;
          if (tempReadsID == 150) {
            break;
          }
          read = reads.get(tempReadsID);
          //System.out.println(refPosition);
        }
        if (mis > 0) {
//          System.out.println("mis:" + mis);
//          System.out.println("sum:" + sum);
//          System.out.println(refPosition);
          if (sum / mis <= 2) {
            Vcf vcf = new Vcf(refPosition, refc, difreadc, sum, mis);
            vcfs.add(vcf);
            vcf.print();
          }
        }    
        refPosition++;
      }
      
    }
    return true;
  }
  
  public void readFromFile() {
    File f=new File("lib/chr22.hg19.fa");
    File f2=new File("lib/SRR8244841.chr22.hg19.1k.sam");
        FileReader fre;
        FileReader fre2;
        try {
          fre = new FileReader(f);
          BufferedReader bre=new BufferedReader(fre);
          fre2 = new FileReader(f2);
          BufferedReader bre2=new BufferedReader(fre2);
          String str="";
          int i = 0;
          StringBuilder refBuilder = new StringBuilder();
          while((str=bre.readLine())!=null) //●判断最后一行不存在，为空
          {
            if(i == 0) {
              i++;
              continue; //pass the head line      
            }
            refBuilder.append(str);
          }
          ref = refBuilder.toString();
          ref = ref.toUpperCase();
          i = 0;
          String pattern = "(XM:i:)(\\d)";
          Pattern p = Pattern.compile(pattern);
          
          while((str=bre2.readLine())!=null) //●判断最后一行不存在，为空
          {
            Matcher m = p.matcher(str);
            if (m.find()) {
              String misNumber = m.group(2);
              //System.out.println(misNumber);
              if (misNumber.compareTo("0") > 0) { //there are some mismatch
                //System.out.println("ooo");
                String[] messages = str.split("\t");
                int position = Integer.valueOf(messages[3]);
                String readStr = messages[9];
                readLength = readStr.length();
                Read read = new Read(readStr, position);
                reads.add(read);
              }
            } else {
//              System.out.println(str);
//              System.out.println(i);
//              System.out.println("can't find the ");
            }
            //System.out.println(str);
            i++;
          }
          
          bre.close();
          fre.close();
          bre2.close();
          fre2.close();
        } catch (FileNotFoundException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }      
  }
}

class Vcf {
  int position;
  char refc;
  char readc;
  int sum;
  int mis;
  public Vcf(int position, char refc, char readc, int sum, int mis) {
    this.position = position;
    this.readc = readc;
    this.refc = refc;
    this.sum = sum;
    this.mis = mis;
  }
  
  public void print() {
    System.out.print(position + "\t");
    System.out.print(refc + "\t");
    System.out.print(readc + "\t");
    System.out.print(sum + "\t");
    System.out.println(mis + "\t");
  }
}

class Read {
  String str; // read string
  int position; // the start position
  public Read(String str, int position) {
    this.str = str;
    this.position = position;
  }
}