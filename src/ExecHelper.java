import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExecHelper implements Runnable {
          // Allocate 1K buffers for Input and Error Streams..
          private byte[] inBuffer = new byte[1024];
          private byte[] errBuffer = new byte[1024];
          // Declare internal variables we will need..
          private Process process;
          private InputStream pErrorStream;
          private InputStream pInputStream;
          private OutputStream pOutputStream;
          private PrintWriter outputWriter;
          private Thread processThread;
          private Thread inReadThread;
          private Thread errReadThread;
          private MainFrame handler;
          private boolean notify;
          /** Private constructor so that no one can create a new ExecHelper directly..
           * @param testFrame MainFrame, that need Infos from the Process
           * @param p Process, that need to be logged
           * @param n true if testFrame.serverStopped Funktion needs to be called after Process termination
           */
          private ExecHelper(MainFrame testFrame, Process p, boolean n) {
                // Save variables..
                handler = testFrame;
                if(handler == null)handler=new MainFrame();
                process = p;
                notify = n;
                // Get the streams..
                pErrorStream = process.getErrorStream();
                pInputStream = process.getInputStream();
                pOutputStream = process.getOutputStream();
                // Create a PrintWriter on top of the output stream..
                outputWriter = new PrintWriter(pOutputStream, true);
                // Create the threads and start them..
                processThread = new Thread(this);
                inReadThread = new Thread(this);
                errReadThread = new Thread(this);
                // Start Threads..
                processThread.start();
                inReadThread.start();
                errReadThread.start();
          }
    
          public boolean isAlive(){
            return processThread.isAlive();
          }
          
          /** Must be called after process Termination
           *  call handler.serverStopped(exitValue) if notify is true
           */
          private void processEnded(int exitValue) {
              if(notify){
                  // Handle process end..
                  handler.serverStopped(exitValue);
              }
          }
    
          // Prepend a current Data to the input, and call handler.processNewInput() with it
          private void processNewInput(String input) {
                // Handle process new input..
                handler.processNewInput((!input.equals("")?
                    new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").
                    format(new Date())+" ":"")+input);
          }
    
          // Call handler.processNewError(error)
          private void processNewError(String error) {
                // Handle process new error..
                handler.processNewError(error);
          }
          
          /** Handle the proccess and return the ExecHelper wrapper object..
           * @param testFrame MainFrame, that need Infos from the Process
           * @param p Process, that need to be handled
           * @param n true if testFrame.serverStopped Funktion needs to be called after Process termination
           */
           public static ExecHelper exec(MainFrame testFrame, Process p, boolean n) throws IOException {
             return new ExecHelper(testFrame, p, n);
           }
    
          /** Run the command and return the ExecHelper wrapper object..
          * @param testFrame MainFrame, that need Infos from the Process
          * @param command Command, that needs to be executed
          * @param n true if testFrame.serverStopped Funktion needs to be called after Process termination
          */
          public static ExecHelper exec(MainFrame testFrame, String command, boolean n) throws IOException {
            return new ExecHelper(testFrame, new ProcessBuilder(command).start(), n);
          }
          
          /** Run the command list and return the ExecHelper wrapper object..
           * @param testFrame MainFrame, that need Infos from the Process
           * @param command Command with arguments, that needs to be executed
           * @param n true if testFrame.serverStopped Funktion needs to be called after Process termination
           */
          public static ExecHelper exec(MainFrame testFrame, List<String> command, boolean n) throws IOException {
            return new ExecHelper(testFrame, new ProcessBuilder(command).start(), n);
          }
    
          // Print the output string through the print writer..
          public void print(String output) {
                outputWriter.print(output);
          }
    
          // Print the output string (and a CRLF pair) through the print writer..
          public void println(String output) {
                outputWriter.println(output);
          }
          
          // Destroy this-Process
          public void stop() {
                process.destroy();
          }
    
          public void run() {
                // Are we on the process Thread?
                if (Thread.currentThread() == processThread) {
                      try {
                            // This Thread just waits for the process to end and notifies the handler..
                            processEnded(process.waitFor());
                      } catch (InterruptedException ex) {
                            ex.printStackTrace();
                      }
                // Are we on the InputRead Thread?
                } else if (Thread.currentThread() == inReadThread) {
                      try {
                            // Read the InputStream in a loop until we find no more bytes to read..
                            for (int i = 0; i > -1; i = pInputStream.read(inBuffer)) {
                                  // We have a new segment of input, so process it as a String..
                                  processNewInput(new String(inBuffer, 0, i));
                            }
                      } catch (IOException ex) {
                            ex.printStackTrace();
                      }
                // Are we on the ErrorRead Thread?
                } else if (Thread.currentThread() == errReadThread) {
                      try {
                            // Read the ErrorStream in a loop until we find no more bytes to read..
                            for (int i = 0; i > -1; i = pErrorStream.read(errBuffer)) {
                                  // We have a new segment of error, so process it as a String..
                                  processNewError(new String(errBuffer, 0, i));
                            }
                      } catch (IOException ex) {
                            ex.printStackTrace();
                      }
                }
          }
    }