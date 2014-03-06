# Introduction

NavTable is a gvSIG's extension to view in a agile way the records of vectorial geographical information layers. Its main characteristic is that allows to see the element's attributes one by one and in vertical direction. Some of its features are: edit alphanumeric values and navigate among the elements of an information layer.

NavTable has been developed by the Cartography Engineering Laboratory of University of A Coruña, CartoLab and it's currently maintained by [iCarto](http://icarto.es) and [CartoLab](http://cartolab.udc.es). It has been released under the terms of the version 3 of the GNU General Public License. We encourage to any user to let us know any suggest, comment, bug reports, etc...

# Installation Instructions

NavTable is a default plugin of gvSIG 1.x, so you should have it installed on your system. If it's not or you want to update it follow the instructions that can be found in the [web of the project](http://navtable.github.io/).

# Version information

NavTable follows [Semantic Versioning](http://semver.org/) style. That is:

Given a version number MAJOR.MINOR.PATCH, increment the:

1. MAJOR version when you make incompatible API changes,
2. MINOR version when you add functionality in a backwards-compatible manner, and
3. PATCH version when you make backwards-compatible bug fixes.

The actual version can be found under the property **version** in file **package.info**. Also, when a new gvsig package (gvspkg) is build the file about.htm that can seen in the about tool of gvSIG will show the version number.

# Build instructions

The code compatibility with the jvm can be found in the **java-version** property of the file **package.info**. Anyway, there are not plans to move it from the actual 1.6 to a higher version.

Setting up a workspace to the gvSIG version specified in the property **gvSIG-version** in **package.info** and then include this project into that workspace.

To build it from the workspace use the ant script contained in the file build.xml to generate the necessary packaging within _fwAndami.

# Packaging instructions

To build a gvsig package (for NavTable devs):

1. Decide the next version number. For example v1.0.4
2. Create a milestone on github, and retag the issues to this milestone
3. Change the appropiate values in the **package.info** file. Usually you will have only to change **version** and **gvSIG-version**
4. Run the target **make-gvsig-pkg** of the **build.xml** ant script. It will replace the placeholder ##VERSION## in the about.htm file and creates a file called navtable-v${version}-for-gvSIG-${gvSIG-version}.gvspkg in /tmp
5. Create a release in github with the same name used in **version** (it will create a new tag automatically)
6. Create a changelog in the release notes
7. Upload the gvspkg to the release
8. Publish the url of the package. It can be used as the url installation in the gvsig plugin manager, or it can be downloaded, unzipped and manually installed

To build a gvsig package (for others):

1. Change the appropiate values in the **package.info** file. Usually you will have only to change **version** and **gvSIG-version**
2. Run the target **make-gvsig-pkg** of the **build.xml** ant script. It will replace the placeholder ##VERSION## in the about.htm file and creates a file called navtable-v${version}-for-gvSIG-${gvSIG-version}.gvspkg in /tmp
3. That's all. You can share the created file with your coworkers.



# Internationalization notes

* Where you can find the translation strings.

Translation strings are inside the files "text_*.properties", where * is the ISO 639-1 language code,
in the "config". The languages that are currently translated NavTable are:


| *Language* | *ISO 639-1 code* |
|------------|------------------|
| Spanish    | es		|
| Galician   | gl	        |
| English    | en	        |
| French     | fr	        |
| Portuguese | pt	        |
| Italian    | it	        |
| German     | de	        |

* What should I do to include a new language

To add a new language, create a file, with the keys of one that already exists, with the language name
followed by an underscore and the ISO 639-1 language code followed by the extension ".properties"

Ex: For Galician: "text_gl.properties"

You can get the list of ISO 639-1 codes on the following link: http://es.wikipedia.org/wiki/ISO_639-1
