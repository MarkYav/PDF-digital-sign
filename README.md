# PDF digital sign
Demonstration how to sign a PDF using certificate

I want to share how to sign a pdf from the very beggining when I haven't forgot everyting.

## First
First thing first it is noce to know what is a pdf signature and how you can make it in other programms.
Here go videos I like:
- What is a pdf signature?: https://www.youtube.com/watch?v=6XsDVx0tjLc&ab_channel=RichardOliverBray
- Example of the signing in other applications: https://www.youtube.com/watch?v=EKpnaQG-LJs&ab_channel=AsoftClick

## Second
Basically, to make a digital sign (not electronic sign) of a PDF you have to have two thing: a PDF to sign and a certificate.
Also, you can create a certificate (I have done it too).
Good article about it:
- How to digitally sign a PDF?: https://medium.com/caution-your-blast/how-to-digitally-sign-a-pdf-programmatically-using-javascript-nodejs-54194af7bdc3

## Third
We will use two libraries: spongycastle and pdfbox-android

The spongycastle is the bouncycastle with little work around so it is easier use on Android.
But it is up to you which one to use. 
It will be used for cryptographic algorithms in order to make your application secure and also to generate a certificate.

THe pdfbox-android is the pdfbox by Apache with little work around so it is easier use on Android. 
There are many other libraries (like iText), but be careful not to mess up with the licence (using iText you have to make your code open source).
It will be used for signing a PDF.

## Fourth
Lets start from creating a new certificate.
I think we should do it first to understand what it is and how it is build.
Good article about it:
- Self Sign Certificate Creation: https://medium.com/@bouhady/self-sign-certificate-creation-using-spongy-castle-for-android-app-61f1545dd63
- Create signature: https://www.tutorialspoint.com/java_cryptography/java_cryptography_creating_signature.htm

## Fifth
As you can see, there are a lot of new things like KeyStore...
It is a good book (if you want to dive in): 
- Secure Key Storage and Secure Computation in Android: https://www.ru.nl/publish/pages/769526/scriptie_tim_cooijmans.pdf

Also, these classes might be usefull too:
- CreateSignature: https://pdfbox.apache.org/docs/2.0.2/javadocs/index.html?org/apache/pdfbox/examples/signature/CreateSignature.html
- KeyStore: https://docs.oracle.com/javase/7/docs/api/java/security/KeyStore.html

## Sixth
Creating a singing.
TO_BE_DONE!!!

Useful matherials:
Create signature: https://www.tutorialspoint.com/java_cryptography/java_cryptography_creating_signature.htm
GitHub issue about the signing: https://github.com/TomRoush/PdfBox-Android/issues/108
Code example signing implementation: https://svn.apache.org/repos/asf/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/signature/CreateSignature.java
(colour version: https://svn.apache.org/viewvc/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/signature/CreateSignature.java?view=markup)
A class that may be used: https://pdfbox.apache.org/docs/2.0.2/javadocs/index.html?org/apache/pdfbox/examples/signature/CreateSignature.html
 

Other:

https://stackoverflow.com/questions/47550563/pdf-signing-with-bouncycastle-using-pdfbox-2-x-x
https://github.com/mkl-public/testarea-pdfbox1/blob/master/src/main/java/mkl/testarea/pdfbox1/sign/CreateSignature.java
https://svn.apache.org/viewvc/pdfbox/branches/2.0/examples/src/main/java/org/apache/pdfbox/examples/signature/
https://gist.github.com/jeff-sleek/9dccc4dfcbe9416608daf0862f75e354
https://www.tutorialspoint.com/java_cryptography/java_cryptography_creating_signature.htm
https://www.baeldung.com/java-digital-signature
https://ec.europa.eu/digital-building-blocks/DSS/webapp-demo/doc/dss-documentation.html
