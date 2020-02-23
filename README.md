## Java class类查找工具

- *从jar包堆中，找出class所在的jar包*

*个人用于在自动化挖掘gadget时，方便查找gadget chains中class所在jar包，以助于便捷审计测试gadget的有效性。*

### 使用帮助：

usage: java -jar findclassinjars.jar [OPTION]
- -c <arg>       需要从jar包堆中找出的类名，例：com.threedr3am.tools.find.jar.Main 或 com/threedr3am/tools/find/jar/Main，多个类名以英文逗号分割
- -full          是否遍历全部文件，缺省则为不全部遍历，从找到该class类后立马结束查找任务
- -h             帮助信息
- -help          帮助信息
- -path <arg>    jar包目录 或 文本文件（可通过正则从文本文件内容提取出jar包路径）
- -regex <arg>   当path指定为目录时，用于正则匹配jar名称是否符合需要，若为文本文件时，用于正则提取jar文件路径
