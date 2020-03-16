package com.threedr3am.tools.find.jar;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author threedr3am
 */
public class Main {

  public static void main(String[] args) throws IOException, ParseException {

    Options options = new Options()
        .addOption("h", false, "帮助信息")
        .addOption("help", false, "帮助信息")
        .addOption("c", true, "需要从jar包堆中找出的类名，例：com.threedr3am.tools.find.jar.Main 或 com/threedr3am/tools/find/jar/Main，多个类名以英文逗号分割")
        .addOption("path", true, "jar包目录 或 文本文件（可通过正则从文本文件内容提取出jar包路径）")
        .addOption("regex", true, "当path指定为目录时，用于正则匹配jar名称是否符合需要，若为文本文件时，用于正则提取jar文件路径")
        .addOption("full", false, "是否遍历全部文件，缺省则为不全部遍历，从找到该class类后立马结束查找任务")
        .addOption("cp", true, "当找到jar后，自动拷贝到该目录")
        ;

    //parser
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    if (cmd.hasOption("h") || cmd.hasOption("help")) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("java -jar find-class-in-jars-1.0-SNAPSHOT-jar-with-dependencies.jar [OPTION]", options);
      return;
    }

    if (!cmd.hasOption("c")) {
      System.err.println("必须指定c参数，更多信息，执行 java -jar find-class-in-jars-1.0-SNAPSHOT-jar-with-dependencies.jar -help");
    }
    if (!cmd.hasOption("path")) {
      System.err.println("必须指定path参数，更多信息，执行 java -jar find-class-in-jars-1.0-SNAPSHOT-jar-with-dependencies.jar -help");
    }
    String cp = cmd.getOptionValue("cp");
    cp = cp.endsWith("/") ? cp : cp + "/";

    String className = cmd.getOptionValue("c");
    String path = cmd.getOptionValue("path");
    String regex = cmd.getOptionValue("regex");
    Pattern pattern = Pattern.compile(regex);
    boolean fullMode = true;

    List<File> files = new ArrayList<>();
    if (Files.isDirectory(Paths.get(path))) {
      Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
          if (file.getFileName().toString().endsWith(".jar") && regex.length() > 0 && pattern
              .matcher(file.getFileName().toString()).find()) {
            File readFile = file.toFile();
            files.add(readFile);
          }
          return FileVisitResult.CONTINUE;
        }
      });
    } else {
      Path p = Paths.get(path);
      byte[] bytes = Files.readAllBytes(p);
      String text = new String(bytes);
      Matcher matcher = pattern.matcher(text);
      while (matcher.find()) {
        for (int i = 0; i < matcher.groupCount(); i++) {
          String jar = matcher.group(i);
          if (jar.endsWith(".jar") && Files.exists(Paths.get(jar))) {
            files.add(Paths.get(jar).toFile());
          }
        }
      }
    }

    if (files.size() == 0) {
      System.err.println("请确认输入参数是否有误，无法读取到任何jar包！");
    }

    className = className.replace("/", ".");
    System.out.println("开始查找类：" + className);
    String[] classNames = className.split(",");
    for (File jar : files) {
      Set<String> classSet = new HashSet<>();
      URLClassLoader classLoader = new URLClassLoader(new URL[]{jar.toURL()});
      String name;
      for (int i = 0; i < classNames.length; i++) {
        name = classNames[i];
        try {
          Class clazz;
          if ((clazz = classLoader.loadClass(name)) != null) {
            classSet.add(clazz.getName());
          }
        } catch (Exception e) {
        } catch (Error e) {
          classSet.add(name);
          System.err.print("该类在此jar包，但缺少其他引用依赖");
        }
        if (!classSet.isEmpty()) {
          System.out.println("----------------------------------------------------------------------");
          System.out.print(jar.getName() + ":\n");
          for (String c : classSet) {
            System.out.println(c);
          }
          System.out.println();
          if (cp != null && !cp.isEmpty()) {
            Files.copy(jar.toPath(), Paths.get(cp + jar.getName()));
          }
        }
      }
      if (!fullMode && classNames.length == 1) {
        break;
      }
    }
  }
}
