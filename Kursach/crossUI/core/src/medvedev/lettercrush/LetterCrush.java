package medvedev.lettercrush;

import com.idp.engine.App;

public class LetterCrush extends App {
    @Override
    public void create(){
        super.create();
    }

    public static LetterCrush getInstance() {
        return (LetterCrush) instance;
    }
}
