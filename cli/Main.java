
import soot.options.Options;
import soot.OptionsParseException;
import soot.PackManager;
import soot.Pack;
import soot.Transform;
import soot.CompilationDeathException;
import soot.PhaseOptions;
import soot.Scene;
import soot.options.CGOptions;
import soot.SootClass;
import soot.util.Chain;
import soot.util.HashChain;
import soot.ClassProvider;
import soot.DexClassProvider;
import soot.SourceLocator;
import soot.SootMethod;
import soot.Body;
import soot.tagkit.InnerClassTagAggregator;
import soot.toolkits.scalar.UnusedLocalEliminator;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import com.sbrella.frontend.cafedragon.CappuccinoDragon;
import com.sbrella.frontend.cafedragon.Configuration;


public class Main {

	private static void processCmdLine(String[] args) {

		if (!Options.v().parse(args))
			throw new OptionsParseException("Option parse error");

		if (PackManager.v().onlyStandardPacks()) {
			for (Pack pack : PackManager.v().allPacks()) {
				Options.v().warnForeignPhase(pack.getPhaseName());
				for (Transform tr : pack) {
					Options.v().warnForeignPhase(tr.getPhaseName());
				}
			}
		}
		Options.v().warnNonexistentPhase();

		if (Options.v().help()) {
			System.out.println(Options.v().getUsage());
			throw new CompilationDeathException(CompilationDeathException.COMPILATION_SUCCEEDED);
		}

		if (Options.v().phase_list()) {
			System.out.println(Options.v().getPhaseList());
			throw new CompilationDeathException(CompilationDeathException.COMPILATION_SUCCEEDED);
		}

		if (!Options.v().phase_help().isEmpty()) {
			for (String phase : Options.v().phase_help()) {
				System.out.println(Options.v().getPhaseHelp(phase));
			}
			throw new CompilationDeathException(CompilationDeathException.COMPILATION_SUCCEEDED);
		}

		if ((!Options.v().unfriendly_mode() && (args.length == 0)) || Options.v().version()) {
			throw new CompilationDeathException(CompilationDeathException.COMPILATION_SUCCEEDED);
		}

		if(Options.v().on_the_fly()) {
			Options.v().set_whole_program(true);
			PhaseOptions.v().setPhaseOption("cg", "off");
		}

		postCmdLineCheck();
	}

	private static void postCmdLineCheck() {
		if (Options.v().classes().isEmpty()
				&& Options.v().process_dir().isEmpty()) {
			throw new CompilationDeathException(
					CompilationDeathException.COMPILATION_ABORTED,
					"No input classes specified!");
		}
	}

	public static void autoSetOptions() {
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_src_prec(Options.src_prec_apk);

		//when no-bodies-for-excluded is enabled, also enable phantom refs
		if(Options.v().no_bodies_for_excluded())
			Options.v().set_allow_phantom_refs(true);

		//when reflection log is enabled, also enable phantom refs
		CGOptions cgOptions = new CGOptions( PhaseOptions.v().getPhaseOptions("cg") );
		String log = cgOptions.reflection_log();
		if((log!=null) && (log.length()>0)) {
			Options.v().set_allow_phantom_refs(true);
		}

		//if phantom refs enabled,  ignore wrong staticness in type assigner
		if(Options.v().allow_phantom_refs()) {
			Options.v().set_wrong_staticness(Options.wrong_staticness_fix);
		}
	}

    public static void preProcess() {

        Scene.v().loadNecessaryClasses();

        //PackManager.v().runBodyPacks();
        for (SootClass c : Scene.v().getClasses())
        {
            ArrayList<SootMethod> methodsCopy = new ArrayList<SootMethod>(c.getMethods());
            for (SootMethod m : methodsCopy)
            {
                if (!m.isConcrete())     continue;
                Body body = m.retrieveActiveBody();
                //UnusedLocalEliminator.v().transform(body);
                //body.validate();
            }
        }

        // Aggregator for LineNumber Table attribute
        InnerClassTagAggregator.v().internalTransform("", null);
    }

    public static void translate(String outdir, SootClass sc) {
        Configuration conf = new Configuration();
        conf.outputDirectory = new File(outdir);
        conf.omitClassWithoutSource = false;
        CappuccinoDragon d = new CappuccinoDragon(conf);
        DexClassProvider cp = new DexClassProvider();
        SourceLocator.v()
            .setClassProviders(Arrays.asList(new ClassProvider[] {cp}));
        d.outputSingleModule(sc);
    }

    public static void main(String[] args) {
        processCmdLine(args);
        autoSetOptions();
        preProcess();

        //SootClass sc = Scene.v().loadClassAndSupport("Helloworld.dex");
        Chain<SootClass> scs = Scene.v().getClasses();
        for (SootClass sc : scs)
            if (!sc.isJavaLibraryClass())
            {
                System.out.println(sc.getName() + ":" + sc.isPhantomClass());
                translate("piggy", sc);
            }
    }
}
