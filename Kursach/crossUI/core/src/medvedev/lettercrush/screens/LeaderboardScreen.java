package medvedev.lettercrush.screens;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.idp.engine.App;
import com.idp.engine.base.AppUtils;
import com.idp.engine.ui.graphics.actors.Button;
import com.idp.engine.ui.graphics.actors.Text;
import com.idp.engine.ui.graphics.actors.layouts.VerticalLayout;
import com.idp.engine.ui.screens.AppScreen;
import com.idp.engine.ui.screens.layers.MainLayer;
import medvedev.lettercrush.LetterCrush;

import java.util.ArrayList;

public class LeaderboardScreen extends AppScreen {
    public LeaderboardScreen() {
        super();
    }

    ArrayList<String> leaders = new ArrayList<String>();
    ArrayList<Integer> topResults = new ArrayList<Integer>();

    @Override
    public void init() {
        super.init();

        getMainLayer().setContentLayout(MainLayer.LayoutType.Vertical);
        getMainLayer().getContent().setPadding(App.dp2px(50),0,0,0);
        ((VerticalLayout)(getMainLayer().getContent())).setAlign(VerticalLayout.Align.Center);
        hideNavBar();
        //getMainLayer().setBackgroundColor(App.Colors.MAIN);

        Text gamoverText = new Text("РЕКОРДЫ");
        addActor(gamoverText);

        readRecords();

        for(int i = 0; i < leaders.size(); i++) {
            int pos = i + 1;
            Text record = new Text(pos + ". " + leaders.get(i) + " - "+topResults.get(i));
            addActor(record);
        }
        Button back = new Button("НАЗАД");
        back.setSize(App.dp2px(160), App.dp2px(36));
        addActor(back);

        back.addListener(new ActorGestureListener() {
            public void tap(InputEvent event, float x, float y, int count, int button) {
                LetterCrush.setCurrentScreen(new MenuScreen());
            }
        });
    }

    public void readRecords() {
        String[] ss = (String[]) AppUtils.files.readFile(new FileHandle("leaderboard"));
        for(String s : ss) {
            String[] name_res = s.split("@@@@@");
            this.leaders.add(name_res[0]);
            this.topResults.add(Integer.parseInt(name_res[1]));
        }

    }
}

