package utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.IInvokedMethod;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.collections.Lists;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;

public class CustomizedEmailableReport implements IReporter {

	
    // ~ Instance fields ------------------------------------------------------

    private PrintWriter out;
    private int row;
    private Integer testIndex;
    
    // ~ Methods --------------------------------------------------------------

    /** Creates summary of the run */
    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
                               String outdir) {
        try {
            out = createWriter(outdir);
        } catch (IOException e) {
            return;
        }

        startHtml(out);
        generateSuiteSummaryReport(suites);
        generateMethodDetailReport(suites);
        endHtml(out);
        out.flush();
        out.close();
    }

    protected PrintWriter createWriter(String outdir) throws IOException {

        new File(outdir).mkdirs();
        return new PrintWriter(new BufferedWriter(new FileWriter(new File(outdir, "custom-emailable-report.html"))));

    }

    /** Creates a section showing known results for each method */
    protected void generateMethodDetailReport(List<ISuite> suites) {
        for (ISuite suite : suites) {
            Map<String, ISuiteResult> r = suite.getResults();
            for (ISuiteResult r2 : r.values()) {
                ITestContext testContext = r2.getTestContext();
                resultDetail(testContext.getFailedConfigurations());
                resultDetail(testContext.getFailedTests());
                resultDetail(testContext.getSkippedConfigurations());
                resultDetail(testContext.getSkippedTests());
                resultDetail(testContext.getPassedTests());
            }
        }
    }

    private String millisToTimeConversion(long seconds) {

        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        int minutes = (int) (seconds / SECONDS_IN_A_MINUTE);
        seconds -= minutes * SECONDS_IN_A_MINUTE;

        int hours = minutes / MINUTES_IN_AN_HOUR;
        minutes -= hours * MINUTES_IN_AN_HOUR;

        return prefixZeroToDigit(hours) + ":" + prefixZeroToDigit(minutes) + ":" + prefixZeroToDigit((int)seconds);
    }

    private String prefixZeroToDigit(int num){
        int number=num;
        if(number<=9){
            String sNumber="0"+number;
            return sNumber;
        }
        else
            return ""+number;

    }

    private void resultDetail(IResultMap tests) {

        Set<ITestResult> testResults=tests.getAllResults();
        List<ITestResult> list = new ArrayList<ITestResult>(testResults);
        Collections.sort(list, new TestResultsSorter());
        for (ITestResult result : list) {
            ITestNGMethod method = result.getMethod();
            Set<ITestResult> resultSet = tests.getResults(method);
            generateForResult(result, method, resultSet.size());       
        }
    }

    private void generateForResult(ITestResult ans, ITestNGMethod method,
                                   int resultSetSize) {
        Object[] parameters = ans.getParameters();
        boolean hasParameters = parameters != null && parameters.length > 0;
        if (hasParameters) {
//            tableStart("result", null);
        }
        List<String> msgs = Reporter.getOutput(ans);
        boolean hasReporterOutput = msgs.size() > 0;
        Throwable exception = ans.getThrowable();
        boolean hasThrowable = exception != null;
        if (hasReporterOutput || hasThrowable) {
            if (hasParameters) {
                out.print("<tr><td");
                if (parameters.length > 1) {
                    out.print(" colspan=\"" + parameters.length + "\"");
                }
                out.println(">");
            } else {
                out.println("<div>");
            }
            if (hasReporterOutput) {
                if (hasThrowable) {
                    out.println("<h3>Test Messages</h3>");
                }
                for (String line : msgs) {
                    out.println(line + "<br/>");
                }
            }
            if (hasThrowable) {
                boolean wantsMinimalOutput = ans.getStatus() == ITestResult.SUCCESS;
                if (hasReporterOutput) {
                    out.println("<h3>"
                            + (wantsMinimalOutput ? "Expected Exception"
                            : "Failure") + "</h3>");
                }
                generateExceptionReport(exception, method);
            }
            if (hasParameters) {
                out.println("</td></tr>");
            } else {
                out.println("</div>");
            }
        }
        if (hasParameters) {
            out.println("</table>");
        }
    }

    protected void generateExceptionReport(Throwable exception,
                                           ITestNGMethod method) {
        out.print("<div class=\"stacktrace\">");
        out.print(Utils.stackTrace(exception, true)[0]);
        out.println("</div>");
    }

    /**
     * Since the methods will be sorted chronologically, we want to return the
     * ITestNGMethod from the invoked methods.
     */
    private Collection<ITestNGMethod> getMethodSet(IResultMap tests,
                                                   ISuite suite) {
        List<IInvokedMethod> r = Lists.newArrayList();
        List<IInvokedMethod> invokedMethods = suite.getAllInvokedMethods();
        for (IInvokedMethod im : invokedMethods) {
            if (tests.getAllMethods().contains(im.getTestMethod())) {
                r.add(im);
            }
        }
        Collections.sort(r,new TestSorter());
        List<ITestNGMethod> result = Lists.newArrayList();

        for (IInvokedMethod m : r) {
            for(ITestNGMethod temp:result){
                if(!temp.equals(m.getTestMethod()))
                    result.add(m.getTestMethod());
            }
        }

        // Add all the methods that weren't invoked (e.g. skipped) that we
        // haven't added yet

        Collection<ITestNGMethod> allMethodsCollection=tests.getAllMethods();
        List<ITestNGMethod> allMethods=new ArrayList<ITestNGMethod>(allMethodsCollection);
        Collections.sort(allMethods, new TestMethodSorter());
//        System.out.println("After sorting "+allMethods.toString());

        //for (ITestNGMethod m : tests.getAllMethods()) {
        for (ITestNGMethod m : allMethods) {
            //System.out.println("tests.getAllMethods()  .."+m);
            if (!result.contains(m)) {
                result.add(m);
            }
        }
        //System.out.println("results ....."+ result.toString());
        return result;
    }

    @SuppressWarnings("unused")
    public void generateSuiteSummaryReport(List<ISuite> suites) {
        tableStart("testOverview", null);
        out.print("<tr>");
        tableColumnStart("Test");
//        tableColumnStart("# passed");
//        tableColumnStart("# skipped");
//        tableColumnStart("# failed");
        tableColumnStart("Start<br/>Time");
        tableColumnStart("End<br/>Time");
        tableColumnStart("Total<br/>Time(hh:mm:ss)");
//        tableColumnStart("Included<br/>Groups");
//        tableColumnStart("Excluded<br/>Groups");

        out.println("</tr>");
        NumberFormat formatter = new DecimalFormat("#,##0.0");
        int qty_tests = 0;
        int qty_pass_m = 0;
        int qty_pass_s = 0;
        int qty_skip = 0;
        int qty_fail = 0;
        long time_start = Long.MAX_VALUE;
        long time_end = Long.MIN_VALUE;
        testIndex = 1;
        for (ISuite suite : suites) {
            if (suites.size() >= 1) {
                titleRow(suite.getName(), 10);
            }
            Map<String, ISuiteResult> tests = suite.getResults();
            for (ISuiteResult r : tests.values()) {
                qty_tests += 1;
                ITestContext overview = r.getTestContext();
                startSummaryRow(overview.getName());
//                int q = getMethodSet(overview.getPassedTests(), suite).size();
//                qty_pass_m += q;
//                summaryCell(q, Integer.MAX_VALUE);
//				q = getMethodSet(overview.getSkippedTests(), suite).size();
//                qty_skip += q;
//                summaryCell(q, 0);
//                q = getMethodSet(overview.getFailedTests(), suite).size();
//                qty_fail += q;
//                summaryCell(q, 0);

                SimpleDateFormat summaryFormat = new SimpleDateFormat("kk:mm:ss");
                summaryCell(summaryFormat.format(overview.getStartDate()),true);
                out.println("</td>");

                summaryCell(summaryFormat.format(overview.getEndDate()),true);
                out.println("</td>");

                time_start = Math.min(overview.getStartDate().getTime(),
                        time_start);
                time_end = Math.max(overview.getEndDate().getTime(), time_end);
				summaryCell(millisToTimeConversion((overview.getEndDate().getTime() - overview
                        .getStartDate().getTime()) / 1000), true);

//                summaryCell(overview.getIncludedGroups());
//                summaryCell(overview.getExcludedGroups());
                out.println("</tr>");
                testIndex++;
            }
        }
        if (qty_tests > 1) {
            out.println("<tr class=\"total\"><td>Total</td>");
            summaryCell(qty_pass_m, Integer.MAX_VALUE);
            //summaryCell(qty_pass_s, Integer.MAX_VALUE);
            summaryCell(qty_skip, 0);
            summaryCell(qty_fail, 0);
            //summaryCell(" ", true);
            summaryCell(" ", true);
            summaryCell(" ", true);
            //summaryCell(" ", true);
			/*summaryCell(
					formatter.format(((time_end - time_start) / 1000.) / 60.)
					+ " minutes", true);*/
            summaryCell(millisToTimeConversion(((time_end - time_start) / 1000)), true);
            out.println("<td colspan=\"3\">&nbsp;</td></tr>");
        }
        out.println("</table>");
    }


    private void summaryCell(String[] val) {
        StringBuffer b = new StringBuffer();
        for (String v : val) {
            b.append(v + " ");
        }
        summaryCell(b.toString(), true);
    }

    private void summaryCell(String v, boolean isgood) {
        out.print("<td class=\"numi" + (isgood ? "" : "_attn") + "\">" + v
                + "</td>");
    }

    private void startSummaryRow(String label) {
        row += 1;
        out.print("<tr"
                + (row % 2 == 0 ? " class=\"stripe\"" : "")
                + "><td style=\"text-align:left;padding-right:2em\"><a href=\"#t"
                + testIndex + "\"><b>" + label + "</b></a>" + "</td>");

    }

    private void summaryCell(int v, int maxexpected) {
        summaryCell(String.valueOf(v), v <= maxexpected);
    }

    private void tableStart(String cssclass, String id) {
        out.println("<table cellspacing=\"0\" cellpadding=\"0\""
                + (cssclass != null ? " class=\"" + cssclass + "\""
                : " style=\"padding-bottom:2em\"")
                + (id != null ? " id=\"" + id + "\"" : "") + ">");
        row = 0;
    }

    private void tableColumnStart(String label) {
        out.print("<th>" + label + "</th>");
    }

    private void titleRow(String label, int cq) {
        titleRow(label, cq, null);
    }

    private void titleRow(String label, int cq, String id) {
        out.print("<tr");
        if (id != null) {
            out.print(" id=\"" + id + "\"");
        }
        out.println("><th colspan=\"" + cq + "\">" + label + "</th></tr>");
        row = 0;
    }

    /** Starts HTML stream */
    protected void startHtml(PrintWriter out) {
        out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");
        out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        out.println("<head>");
        out.println("<title>TestNG Report</title>");
        out.println("<style type=\"text/css\">");
        out.println("table {margin-bottom:10px;border-collapse:collapse;empty-cells:show}");
        out.println("td,th {border:1px solid #009;padding:.25em .5em}");
        out.println(".result th {vertical-align:bottom}");
        out.println(".param th {padding-left:1em;padding-right:1em}");
        out.println(".param td {padding-left:.5em;padding-right:2em}");
        out.println(".stripe td,.stripe th {background-color: #E6EBF9}");
        out.println(".numi,.numi_attn {text-align:right}");
        out.println(".total td {font-weight:bold}");
        out.println(".passedodd td {background-color: #0A0}");
        out.println(".passedeven td {background-color: #3F3}");
        out.println(".skippedodd td {background-color: #CCC}");
        out.println(".skippedodd td {background-color: #DDD}");
        out.println(".failedodd td,.numi_attn {background-color: #F33}");
        out.println(".failedeven td,.stripe .numi_attn {background-color: #D00}");
        out.println(".stacktrace {white-space:pre;font-family:monospace}");
        out.println(".totop {font-size:85%;text-align:center;border-bottom:2px solid #000}");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
    }

    /** Finishes HTML stream */
    protected void endHtml(PrintWriter out) {
        out.println("</body></html>");
    }

    // ~ Inner Classes --------------------------------------------------------
    /** Arranges methods by classname and method name */
    private class TestSorter implements Comparator<IInvokedMethod> {
        // ~ Methods
        // -------------------------------------------------------------

        /** Arranges methods by classname and method name */
        @Override
        public int compare(IInvokedMethod o1, IInvokedMethod o2) {
            //System.out.println("Comparing " + ((ITestNGMethod) o1).getMethodName() + " " + o1.getDate() + " and " + ((ITestNGMethod) o2).getMethodName() + " " + o2.getDate());
            //return (int) (o1.getDate() - o2.getDate());
            //System.out.println("First method class name "+o1.getTestMethod().getTestClass().getName());
            //System.out.println("second method class name "+o2.getTestMethod().getTestClass().getName());
            int r =o1.getTestMethod().getTestClass().getName().compareTo(o2.getTestMethod().getTestClass().getName());
            //System.out.println("class name compare "+ r);
            if (r == 0) {
                //System.out.println("First method name "+o1.getTestMethod());
                //System.out.println("second method name "+o2.getTestMethod());
                r=o1.getTestMethod().compareTo(o2.getTestMethod());

            }
            return r;
        }

    }

    private class TestMethodSorter implements Comparator<ITestNGMethod> {
        @Override
        public int compare(ITestNGMethod o1, ITestNGMethod o2) {
            //return (int) (o1.getDate() - o2.getDate());
            //System.out.println("First method class name "+o1.getTestClass().getName());
            //System.out.println("second method class name "+o2.getTestClass().getName());
            int r =o1.getTestClass().getName().compareTo(o2.getTestClass().getName());
            //System.out.println("class name compare "+ r);
            if (r == 0) {
                //System.out.println("First method name "+o1.getMethodName());
                //System.out.println("second method name "+o2.getMethodName());
                r=o1.getMethodName().compareTo(o2.getMethodName());
            }
            return r;
        }
    }

    private class TestResultsSorter implements Comparator<ITestResult> {
        @Override
        public int compare(ITestResult o1, ITestResult o2) {
            //return (int) (o1.getMethod().getDate() - o2.getMethod().getDate());
            //System.out.println("First method class name "+o1.getTestClass().getName());
            //System.out.println("second method class name "+o2.getTestClass().getName());
            int result = o1.getTestClass().getName().compareTo(o2.getTestClass().getName());
            if (result == 0) {
                //System.out.println("First method name "+o1.getMethod().getMethodName());
                //System.out.println("second method name "+o2.getMethod().getMethodName());
                result = o1.getMethod().getMethodName().compareTo(o2.getMethod().getMethodName());
            }
            return result;
        }
    }

}
