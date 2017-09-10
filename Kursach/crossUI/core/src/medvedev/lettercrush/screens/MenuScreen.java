package medvedev.lettercrush.screens;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.idp.engine.App;
import com.idp.engine.base.AppUtils;
import com.idp.engine.ui.graphics.actors.Button;
import com.idp.engine.ui.graphics.actors.layouts.VerticalLayout;
import com.idp.engine.ui.screens.AppScreen;
import com.idp.engine.ui.screens.layers.MainLayer;
import medvedev.lettercrush.LetterCrush;

import java.io.IOException;
import java.util.ArrayList;

public class MenuScreen extends AppScreen {

    private Button newGame;
    private Button continueGame;
    private Button records;

    public MenuScreen() {
        super();
    }

    @Override
    public void init() {
        super.init();


        getMainLayer().setContentLayout(MainLayer.LayoutType.Vertical);
        getMainLayer().getContent().setPadding(App.dp2px(50),0,0,0);
        ((VerticalLayout)(getMainLayer().getContent())).setAlign(VerticalLayout.Align.Center);
        hideNavBar();
        getMainLayer().setBackgroundColor(Color.WHITE);

        float buttonWidth = App.dp2px(160);
        float buttonHeight = App.dp2px(36);


        newGame = new Button("НОВАЯ ИГРА");
        newGame.setSize(buttonWidth, buttonHeight);
        addActor(newGame);

        newGame.addListener(new ActorGestureListener() {
            public void tap(InputEvent event, float x, float y, int count, int button) {
                LetterCrush.setCurrentScreen(new GameScreen());
            }
        });

        continueGame = new Button("ПРОДОЛЖИТЬ");
        continueGame.setSize(buttonWidth, buttonHeight);
        addActor(continueGame);

        continueGame.addListener(new ActorGestureListener() {
            public void tap(InputEvent event, float x, float y, int count, int button) {
                loadXML();
            }
        });

        records = new Button("РЕКОРДЫ");
        records.setSize(buttonWidth, buttonHeight);
        addActor(records);

        records.addListener(new ActorGestureListener() {
            public void tap(InputEvent event, float x, float y, int count, int button) {
                LetterCrush.setCurrentScreen(new LeaderboardScreen());
            }
        });

    }

    ArrayList<String> loadCont() {
        return (ArrayList<String>) AppUtils.files.readFile(AppUtils.files.internal("cont"));
    }

    Integer loadLastScore() {
        return (Integer) AppUtils.files.readFile(AppUtils.files.internal("lastscore"));
    }

    void loadXML() {

        XmlReader xr = new XmlReader();
        try {
            XmlReader.Element save = xr.parse(AppUtils.files.local("xmlsave"));
            LetterCrush.setCurrentScreen(new GameScreen(
                    save.getChildrenByName("letter"),
                    Integer.parseInt(save.getAttribute("lastScore"))
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
