# DocxToBB
The DocxToBB conversion tool converts specially formatted Word documents to Blackboard Learn test packages. The goal of this project is to maintain exams in a format that is both printable for the F2F modality, and able to be parsed to generate a Blackboard Learn compatible package. This tool thus rapidly facilitates the process of moving from F2F to online testing.

Most converters that accept .doc(x) or .txt files:
1. Are limited in what types of resources (i.e. images, equations, etc) can be input
1. Do not have a visually appealing printable form
1. Do not accept nested questions that share a common instruction
1. Do not remove necessary indexes from the printable form when converting to the online form **[See Note 1]**
1. Do not handle vector-based resources (i.e. WMF) at all
1. Are cost prohibitive

> Note 1: Feature coming soon...

DocxToBB handles most of these shortcomings allowing flexibility and functionality to take priority.

## Requirements
### For Running
- Java 8 (untested)
- Java 11 or higher
### For Building
- Git
- Gradle 6.4.1 or later

## Additional Notes
1. DocxToBB is still a W.I.P. and needs testing before production use.

## TODO List
- [ ] Add better error reporting interfaces
- [ ] Generate a .txt log of process
- [ ] Test handling of image resources
- [ ] Write users manual (i.e. Wiki)