//import java.util.*;
//import ece351.w.ast.*;
//import ece351.w.parboiled.*;
//import static ece351.util.Boolean351.*;
//import ece351.util.CommandLine;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.StringWriter;
//import java.io.PrintWriter;
//import java.io.IOException;
//import ece351.util.Debug;
//
//public final class Simulator_cse0 {
//    public static void main(final String[] args) {
//        // read the input F program
//        // write the output
//        // read input WProgram
//        // construct storage for output
//        // loop over each time step
//        // values of input variables at this time step
//        // values of output variables at this time step
//        // store outputs
//        // write the input
//        // write the output
//        final CommandLine cmd = new CommandLine(args);
//        final String input = cmd.readInputSpec();
//        final WProgram wprogram = WParboiledParser.parse(input);
//        final Map<String,StringBuilder> output = new LinkedHashMap<String,StringBuilder>();
//        output.put("z", new StringBuilder());
//        output.put("x", new StringBuilder());
//        output.put("t", new StringBuilder());
//        output.put("y", new StringBuilder());
//        output.put("r", new StringBuilder());
//        final int timeCount = wprogram.timeCount();
//        for (int time = 0; time < timeCount; time++) {
//        final boolean in_a = wprogram.valueAtTime("a", time);
//        final boolean in_b = wprogram.valueAtTime("b", time);
//        final boolean in_c = wprogram.valueAtTime("c", time);
//        final String out_z = z() ? "1 " : "0 ";
//        output.get("z").append(out_z);
//        final String out_x = x(in_a,in_b) ? "1 " : "0 ";
//        output.get("x").append(out_x);
//        final String out_t = t(in_a,in_b,in_c) ? "1 " : "0 ";
//        output.get("t").append(out_t);
//        final String out_y = y(in_a,in_b) ? "1 " : "0 ";
//        output.get("y").append(out_y);
//        final String out_r = r(in_c,in_b,in_a) ? "1 " : "0 ";
//        output.get("r").append(out_r);
//        try {
//            final File f = cmd.getOutputFile();
//            f.getParentFile().mkdirs();
//            final PrintWriter pw = new PrintWriter(new FileWriter(f));
//            System.out.println(wprogram.toString());
//            pw.println(wprogram.toString());
//            System.out.println(f.getAbsolutePath());
//            for (final Map.Entry<String,StringBuilder> e : output.entrySet()) {
//                System.out.println(e.getKey() + ":" + e.getValue().toString() + ";");
//                pw.write(e.getKey() + ":" + e.getValue().toString() + ";\n");
//            }
//            pw.close();
//        } catch (final IOException e) {
//            Debug.barf(e.getMessage());
//        }
//        // methods to compute values for output pins
//    }
//    }}
