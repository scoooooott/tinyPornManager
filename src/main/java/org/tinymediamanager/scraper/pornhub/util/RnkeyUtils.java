package org.tinymediamanager.scraper.pornhub.util;

import java.util.LinkedList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RnkeyUtils {

    private final static LinkedList<String> keyPool = new LinkedList<>();
    private final static Pattern RNKEY_HTML_PATTERN = Pattern.compile("(.*)(function leastFactor\\(n\\).*)(function go\\(\\) \\{ )(.*)(n=leastFactor\\(p\\);\\{)(.*?=)(.*?;)(.*)");
    private static ScriptEngine javaScriptEngine = null;

    public static String nextKey() {
        int next = new Random().nextInt(keyPool.size());
        return keyPool.get(next);
    }

    static {
        keyPool.add("2832329*4704551:4259017186:1074632092:1");
        keyPool.add("4022903*5367977:4063241930:1340522969:1");
        keyPool.add("3864089*4174873:1760808845:3575982077:1");
        keyPool.add("2917037*2937757:335342078:2921989523:1");
        keyPool.add("3221899*3808327:341031270:2844087510:1");
        keyPool.add("2689601*2698321:1520587235:3883226924:1");
        keyPool.add("4168741*5061857:2900222849:285928235:1");
        keyPool.add("4524607*6147131:233016430:2956232214:1");
        keyPool.add("4290823*5007791:1494207740:3839410404:1");
        keyPool.add("7557167*8225929:3185077567:1013941:1");
        keyPool.add("7557167*8225929:3185077567:1013941:1");
        keyPool.add("3820153*4708381:2314890721:875394895:1");
        keyPool.add("3035059*3728293:2643426132:542789035:1");
        keyPool.add("3221899*3808327:341031270:2844087510:1");
        keyPool.add("2903893*4159403:2471765288:780352483:1");

        ScriptEngineManager factory = new ScriptEngineManager();
        javaScriptEngine = factory.getEngineByName("JavaScript");
    }

    public static void genRnKey(String html) throws ScriptException {
        Matcher htmlMatcher = RNKEY_HTML_PATTERN.matcher(html);
        if (htmlMatcher.find()) {
            String functionP = htmlMatcher.group(4);
            String functionF = htmlMatcher.group(2);
            String functionRNKEY = htmlMatcher.group(7);
            String p = javaScriptEngine.eval(functionP).toString();

            String rnkey = functionRNKEY
                .replace("p", p)
                .replace("n", javaScriptEngine.eval(functionF + "leastFactor(" + p + ")").toString());

            if (keyPool.size() != 0 && keyPool.size() > 20) {
                keyPool.removeLast();
            }
            keyPool.push(javaScriptEngine.eval(rnkey).toString().replace("RNKEY=", ""));
        }
    }

    public static void main(String[] args) throws ScriptException {
        String html = "<html><head><script type=\"text/javascript\"><!--function leastFactor(n){if(isNaN(n)||!isFinite(n))return NaN;if(typeof phantom!=='undefined')return'phantom';if(typeof module!=='undefined'&&module.exports)return'node';if(n==0)return 0;if(n%1||n*n<2)return 1;if(n%2==0)return 2;if(n%3==0)return 3;if(n%5==0)return 5;var m=Math.sqrt(n);for(var i=7;i<=m;i+=30){if(n%i==0)return i;if(n%(i+4)==0)return i+4;if(n%(i+6)==0)return i+6;if(n%(i+10)==0)return i+10;if(n%(i+12)==0)return i+12;if(n%(i+16)==0)return i+16;if(n%(i+22)==0)return i+22;if(n%(i+24)==0)return i+24}return n}function go(){var p=10476136087962;var s=708213922;var n;if((s>>12)&1)p+=116336241*15;else p-=77761496*13;if((s>>9)&1)p+=13183986*10;else p-=175094190*10;if((s>>11)&1)p+=159117913*12;else p-=109116614*12;if((s>>10)&1)p+=25374633*13;else p-=12249991*11;if((s>>11)&1)p+=51502035*12;else p-=15899697*12;p-=3066911399;n=leastFactor(p);{document.cookie=\"RNKEY=\"+n+\"*\"+p/n+\":\"+s+\":3707317501:1; path=/\";document.location.reload(true)}}";
        RnkeyUtils.genRnKey(html);
    }
}
