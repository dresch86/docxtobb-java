# DocxToBB
The DocxToBB conversion tool converts specially formatted Word documents to Blackboard Learn test packages. The goal of this project is to maintain exams in a format that is both printable for the F2F modality, and able to be parsed to generate a Blackboard Learn compatible package. This tool thus rapidly facilitates the process of moving from F2F to online testing.

Most converters that accept .doc(x) or .txt **[See Note 1]** files:
1. Are limited in what types of resources (i.e. images, equations **[See Note 2]**, etc) can be input
1. Do not consume a visually appealing printable form
1. Do not accept nested questions that share a common instruction
1. Do not remove necessary indexes from the printable form when converting to the online form
1. Do not handle vector-based resources (i.e. WMF) at all
1. Are proprietary

> **Note 1:** DocxToBB does not accept .txt files as input since many tools already work well with the limitations of that file format
> 
> **Note 2:** Feature coming soon...

DocxToBB handles most of these shortcomings allowing flexibility and functionality to take priority.

## Tutorials
[Installing and Creating Test Packages](https://youtu.be/4PlM02fr86s)  
[Uploading Test Packages](https://youtu.be/uFkbHT1vVq8)

## FAQ
**Q: What is the goal of this project?**  
*A: To serve as a tool allowing educators to easily adapt their examination materials from a face to face to online environment using a common functional format.*  

**Q: Couldn't I just use the Blackboard web interface to create an exam?**  
*A: Yes, you can! The interface is clunky however. It is a lot easier to type an exam in Word, and then review it by scrolling to make edits as opposed to clicking through each question in a list. Dragging and dropping resources like images is a lot easier in Word as well. Additionally, many vector art tools that support round trip editing will not copy/paste into a web browser since most browsers cannot display legacy WMF files.*  

**Q: How does DocxToBB compare to other tools?**  
*A: DocxToBB is intended to convert a printable exam document to a Blackboard Learn Test (not a pool). Most other tools do not handle embedded images, especially vector (i.e. WMF) ones, at all. The inability to embed images is particularly challenging for visual subjects like chemistry, biology, physics, engineering, and art.*  

**Q: How does DocxToBB work?**  
*A: DocxToBB compatible Word files require boxing questions into tables, which are automatically detected and parsed for content. To see examples of how questions can be structured, see the [examples directory](https://github.com/dresch86/docxtobb-java/tree/master/examples).*  

**Q: What happens to content outside the question boxes?**  
*A: It gets ignored.*  

**Q: Can responses contain images too?**  
*A: Yes, they can. There are two ways the images can be included, as attachments or embedded. See the [Wiki](https://github.com/dresch86/docxtobb-java/wiki) for more information.*  

**Q: Can question numerical indexes needed for in person exams be removed for online exams to allow for question randomization?**  
*A: Yes, there is a checkbox to select that will scan each question for the following index types ("**N.**", "**N)**", "**#N.**", and "**#N)**") where N is a positive integer number.*  

**Q: Are these formatting rules really necessary?**  
*A: Yes, they are. A document parser cannot figure out where your content is or how it should be treated on its own. Predictable formatting cues are the only way for the program to know the difference between a question and an answer.*  

**Q: Does DocxToBB work with a LMS other than Blackboard Learn?**  
*A: Much of the XML structure contained within the test package is QTI-based however Blackboard Learn adds some of its own tags especially for image / resource handling. You are free to try the output package on your LMS, and let me know if it works. I may consider adding support for other LMS if the changes needed are minor AND you can provide a way for me to test the output.*  

**Q: Does DocxToBB convert Word equations correctly?**  
*A: This feature is currently under development. The underlying library, [java-mammoth](https://github.com/mwilliamson/java-mammoth) that does most of the heavy lifting here does not handle conversion of equations. I have a [fork](https://github.com/dresch86/java-mammoth) I am working on to convert the Word equations to MathML, but it is going to take some effort to get it right / complete. In the mean time, the best thing to do is add the equations as images, or in vector format if you have software capable of roundtrip editing.*  

**Q: Do you offer technical support?**  
*A: At this time, DocxToBB is an open source project and I strongly prefer it to be community driven. If you need assistance, please create an [Issue](https://github.com/dresch86/docxtobb-java/issues) here on GitHub. Response times will vary, but a community user may have as good advise or better than I.*  

**Q: Can I request a feature?**  
*A: Yes, please create an [Issue](https://github.com/dresch86/docxtobb-java/issues) here on GitHub. Remember, this is and will be a community-driven effort so a request does not guarantee fulfillment. I will happily accept pull requests though! :-)*  

**Q: Can I run DocxToBB on MacOS?**  
*A: Since the program is Java based it will run on any platform that can run Java. Travis CI/CD builds the .dmg and .pkg files for release however they are not signed by Apple since I do not have an Apple Developers Account. If you try to run the macOS files under releases you will get an error that they are damaged when they are not. To install unsigned macOS applications please see the [following link](https://secure.clcbio.com/helpspot/index.php?pg=kb.page&id=323).*  

## Requirements
### For Running
- Windows 7+
- macOS
- Debian Based Linux
- Other Linux Distros (comming soon)
### For Building
- Git
- Java 13 (for packaging) or higher
- JavaFX
- Gradle 6.4.1 or later

## TODO List
- [X] Add better error reporting interfaces
- [ ] Generate a log file of process
- [X] Test handling of image resources
- [X] Create video tutorials
- [ ] Write detailed user manual (i.e. Wiki)
- [X] Add release version

## Patreon
If you would like to support this project please consider becoming a [Patreon](https://www.patreon.com/dresch86). Patreons have additional perks in steering the project direction / feature set.