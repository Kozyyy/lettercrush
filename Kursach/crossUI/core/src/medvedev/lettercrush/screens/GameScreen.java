package medvedev.lettercrush.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlWriter;
import com.idp.engine.App;
import com.idp.engine.base.AppUtils;
import com.idp.engine.ui.graphics.actors.Button;
import com.idp.engine.ui.graphics.actors.FieldText;
import com.idp.engine.ui.graphics.actors.Text;
import com.idp.engine.ui.graphics.actors.layouts.VerticalLayout;
import com.idp.engine.ui.graphics.base.Rect;
import com.idp.engine.ui.screens.AppScreen;
import com.idp.engine.ui.screens.layers.MainLayer;

import medvedev.lettercrush.LetterCrush;
import medvedev.lettercrush.actors.GameButton;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

public class GameScreen extends AppScreen{

    GameScreen() {
        super("GAME");
    }

    GameScreen(Array<XmlReader.Element> contList, int lastscore) {
        super("GAME");
        score = lastscore;
        for(int i = 0; i < 6; i++) {
            gbs.add(new ArrayList<GameButton>());
            for(int j = 0; j < 20; j++) {
                gbs.get(i).add(null);
            }
        }
        is_continued = true;
        for(XmlReader.Element str : contList) {
            //String[] str_split = str.split("#");
            final GameButton gb = new GameButton(str.getAttribute("text"));
            gb.setX(Float.parseFloat(str.getAttribute("x")));
            gb.setY(Float.parseFloat(str.getAttribute("y")));
            gb.setSize(square, square);
            if(Boolean.parseBoolean(str.getAttribute("is_flying"))) {
                gb.flies = true;
            }
            else {
                gb.flies = false;
            }
            final ActorGestureListener listener = new ActorGestureListener() {
                public void tap(InputEvent event, float x, float y, int count, int button) {
                    if(!gb.is_checked()) {
                        textBox.setText(textBox.getText() + gb.getText());
                        gb.check();
                    }
                }
            };
            gb.addListener(listener);
            gbs.get(Integer.parseInt(str.getAttribute("i"))).set(Integer.parseInt(str.getAttribute("j")), gb);
        }
        for(ArrayList<GameButton> list : gbs) {
            list.removeAll(Collections.singleton(null));
        }

    }

    Boolean is_continued = false;
    String [] dict;
    int score = 0;
    float square = Gdx.graphics.getWidth() / 6;
    float columnHeight = Gdx.graphics.getHeight() / square - 1;
    float v = 40f;

    GameScreen cont = this;

    ArrayList<ArrayList<GameButton>> gbs = new ArrayList<ArrayList<GameButton>>();

    ArrayList<String> leaders = new ArrayList<String>();
    ArrayList<Integer> topResults = new ArrayList<Integer>();

    Button down;
    Button textBox;
    Button pause;
    Text scoreText;

    float buttonWidth = App.dp2px(160);
    float buttonHeight = App.dp2px(36);



    @Override
    public void init() {
        super.init();
        if(!is_continued) {
            for (int i = 0; i < 6; i++) {
                gbs.add(new ArrayList<GameButton>());
            }
        }

        readDict();
        readRecords();
        getMainLayer().setContentLayout(MainLayer.LayoutType.Absolute);
        this.hideNavBar();

        if(is_continued) {
            for(ArrayList<GameButton> arrayList : gbs) {
                for(GameButton gb : arrayList) {
                    addActor(gb);
                }
            }
        }


        float bottomButtonsY = Gdx.graphics.getHeight() - square;

        down = new Button("V");
        down.setBackgroundColor(Color.GRAY);
        down.setSize(square, square);
        down.setPosition(0, bottomButtonsY);
        down.addListener(new ActorGestureListener() {
            public void tap(InputEvent event, float x, float y, int count, int button) {
                for(final ArrayList<GameButton> list : gbs) {
                    for(GameButton gb : list) {
                        if(gb.is_flying()) {
                            gb.clearActions();
                            MoveToAction action = new MoveToAction();
                            action.setX(gb.getX());
                            int n_squares = 0;
                            for(int k = 0; k < gbs.get(gbs.indexOf(list)).size(); k++) {
                                if (gbs.get(gbs.indexOf(list)).get(k) != null) {
                                    n_squares++;
                                }
                            }
                            action.setY(Gdx.graphics.getHeight() - square * (n_squares + 1));

                            action.setDuration(0f);


                            final GameButton gbf = gb;
                            Action generateNext = new Action() {
                                public boolean act( float delta ) {
                                    gbf.flies = false;
                                    if(gbs.get(gbs.indexOf(list)).size() > columnHeight)
                                        gameOver();
                                    else
                                        generateGameButton();
                                    return true;
                                }
                            };
                            Action actions = new SequenceAction(action, generateNext);
                            gb.addAction(actions);
                        }
                    }
                }
            }
        });
        addActor(down);


        textBox = new Button("");
        textBox.setBackgroundColor(Color.GRAY);
        textBox.setSize(square * 4, square);
        textBox.setPosition(square, bottomButtonsY);
        textBox.addListener(new ActorGestureListener() {
            public void tap(InputEvent event, float x, float y, int count, int button) {
                if(Arrays.asList(dict).contains(textBox.getText().toLowerCase())) {
                    score += Math.pow(2, textBox.getText().length());
                    scoreText.setText("Счет: "+score);
                    textBox.setText("");
                    for(int i=0; i < 6; i++) {
                        for(int j = 0; j < gbs.get(i).size(); j ++) {
                            if(gbs.get(i).get(j)!=null && gbs.get(i).get(j).is_checked()) {
                                if(gbs.get(i).get(j).is_flying()) {
                                    generateGameButton();
                                }
                                gbs.get(i).get(j).remove();

                                gbs.get(i).set(j, null);
                            }
                        }
                    }

                    compress();
                }
                else {
                    textBox.setText("");
                    for(Actor actor : getMainLayer().getContent().getChildren()) {
                        if(actor.getClass().toString().endsWith("GameButton")) {
                            if(((GameButton)actor).is_checked()){
                                ((GameButton) actor).uncheck();
                            }
                        }
                    }
                }
            }
        });
        addActor(textBox);

        scoreText = new Text("Счет: "+score);
        scoreText.setScale(0.8f);
        Label.LabelStyle ls = scoreText.getStyle();
        ls.fontColor = Color.YELLOW;
        scoreText.setStyle(ls);
        scoreText.setPosition(textBox.getX(), textBox.getY());
        addActor(scoreText);


        pause = new Button("||");
        pause.setBackgroundColor(Color.GRAY);
        pause.setSize(square, square);
        pause.setPosition(square * 5, bottomButtonsY);
        pause.addListener(new ActorGestureListener() {
            public void tap(InputEvent event, float x, float y, int count, int button) {
                for(final ArrayList<GameButton> list : gbs) {
                    for (GameButton gb : list) {
                        if (gb.is_flying()) {
                            gb.clearActions();
                        }
                    }
                }
                final Rect rect = new Rect();
                Color rectColor = Color.BLACK;
                rectColor.a = 0.9f;
                rect.setBackgroundColor(rectColor);
                rect.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                addActor(rect);


                final VerticalLayout vL = new VerticalLayout();
                vL.setAlign(VerticalLayout.Align.Center);
                vL.setPosition(Gdx.graphics.getWidth() / 2, vL.getY());

                Text pauseText = new Text("ПАУЗА");
                vL.addActor(pauseText);

                Button resume = new Button("ПРОДОЛЖИТЬ");
                resume.setSize(buttonWidth, buttonHeight);
                resume.addListener(new ActorGestureListener() {
                    public void tap(InputEvent event, float x, float y, int count, int button) {
                        rect.remove();
                        vL.remove();

                        for(final ArrayList<GameButton> list : gbs) {
                            for (GameButton gb : list) {
                                if (gb.is_flying()) {
                                    MoveToAction action = new MoveToAction();

                                    action.setX(gb.getX());
                                    int n_squares = 0;
                                    for (int k = 0; k < gbs.get(gbs.indexOf(list)).size(); k++) {
                                        if (gbs.get(gbs.indexOf(list)).get(k) != null) {
                                            n_squares++;
                                        }
                                    }
                                    action.setY(Gdx.graphics.getHeight() - square * (n_squares + 1));

                                    action.setDuration(Math.abs(Gdx.graphics.getHeight() - gb.getY() - square * (n_squares + 1)) / v);


                                    final GameButton gbf = gb;
                                    Action generateNext = new Action() {
                                        public boolean act(float delta) {
                                            gbf.flies = false;
                                            if (gbs.get(gbs.indexOf(list)).size() > columnHeight)
                                                gameOver();
                                            else
                                                generateGameButton();
                                            return true;
                                        }
                                    };
                                    Action actions = new SequenceAction(action, generateNext);
                                    gb.addAction(actions);
                                }
                            }
                        }
                    }
                });
                vL.addActor(resume);

                Button restart = new Button("ЗАНОВО");
                restart.setSize(buttonWidth, buttonHeight);
                restart.addListener(new ActorGestureListener() {
                    public void tap(InputEvent event, float x, float y, int count, int button) {
                        LetterCrush.setCurrentScreen(new GameScreen());
                    }
                });
                vL.addActor(restart);

                Button goMenu = new Button("В МЕНЮ");
                goMenu.setSize(buttonWidth, buttonHeight);
                goMenu.addListener(new ActorGestureListener() {
                    public void tap(InputEvent event, float x, float y, int count, int button) {

                        try {
                            StringWriter writer = new StringWriter();
                            XmlWriter xml = new XmlWriter(writer);
                            XmlWriter xx = xml.element("game").attribute("lastScore", score);
                            for(int i=0; i < 6; i++) {
                                for(int j = 0; j < gbs.get(i).size(); j ++) {
                                    xx.element("letter")
                                            .attribute("i", i)
                                            .attribute("j", j)
                                            .attribute("is_flying", gbs.get(i).get(j).is_flying())
                                            .attribute("text", gbs.get(i).get(j).getText())
                                            .attribute("x", gbs.get(i).get(j).getX())
                                            .attribute("y", gbs.get(i).get(j).getY())
                                            .pop();
                                }
                            }
                            xx.pop();

                            //Gdx.files.local("xmlsave").writeString(writer.toString(), false);

                            AppUtils.files.writeLocalString("xmlsave", writer.toString());
                            //System.out.println(Gdx.files.local("xmlsave").readString());
                            //writeLocalString("xmlsave", writer.toString());




                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ArrayList<String> contList = new ArrayList<String>();
                        for(int i=0; i < 6; i++) {
                            for(int j = 0; j < gbs.get(i).size(); j ++) {



                                contList.add(i+"#"+j+"#"+gbs.get(i).get(j).is_flying()+"#"+gbs.get(i).get(j).getText()
                                        +"#"+gbs.get(i).get(j).getX()+"#"+gbs.get(i).get(j).getY());
                            }
                        }



                        AppUtils.files.writeFile(AppUtils.files.local("cont"), contList);
                        AppUtils.files.writeFile(AppUtils.files.local("lastscore"), score);

                        LetterCrush.setCurrentScreen(new MenuScreen());
                    }
                });
                vL.addActor(goMenu);


                addActor(vL);
            }
        });
        addActor(textBox);
        addActor(pause);
        if(is_continued) {
            for(final ArrayList<GameButton> list : gbs) {
                for(int i=0; i < list.size(); i++) {
                    System.out.println("=============");

                    System.out.println(list.get(i));
                    if (list.get(i).is_flying()) {
                        MoveToAction action = new MoveToAction();

                        action.setX(list.get(i).getX());
                        int n_squares = 0;
                        for (int k = 0; k < gbs.get(gbs.indexOf(list)).size(); k++) {
                            if (gbs.get(gbs.indexOf(list)).get(k) != null) {
                                n_squares++;
                            }
                        }
                        action.setY(Gdx.graphics.getHeight() - square * (n_squares + 1));

                        action.setDuration(Math.abs(Gdx.graphics.getHeight() - list.get(i).getY() - square * (n_squares + 1)) / v);


                        final GameButton gb = list.get(i);
                        Action generateNext = new Action() {
                            public boolean act(float delta) {
                                gb.flies = false;
                                if (gbs.get(gbs.indexOf(list)).size() > columnHeight)
                                    gameOver();
                                else
                                    generateGameButton();
                                return true;
                            }
                        };
                        Action actions = new SequenceAction(action, generateNext);
                        list.get(i).addAction(actions);
                    }
                }
            }
        }
        else {
            generateGameButton();
        }
    }

    public void readDict() {
        String[] s = AppUtils.files.readFileString(AppUtils.files.internal("dic")).split("\n");
        this.dict = s;
    }

    public void readRecords() {
        String[] ss = (String[])AppUtils.files.readFile(AppUtils.files.internal("leaderboard"));
        for(String s : ss) {
            String[] name_res = s.split("@@@@@");
            this.leaders.add(name_res[0]);
            this.topResults.add(Integer.parseInt(name_res[1]));
        }
    }


    public void generateGameButton() {
        Random rnd = new Random();
        final int col = rnd.nextInt(6);
        String chars = "ЙЦУКЕНГШЩЗХЪФЫВАПРОЛДЖЭЯЧСМИТЬБЮ";
        int letter_num = rnd.nextInt(chars.length());
        final GameButton flyingGb = new GameButton(chars.toCharArray()[letter_num] + "");
        flyingGb.setSize(square, square);
        flyingGb.setPosition(square * col, -square);
        flyingGb.flies = true;
        gbs.get(col).add(flyingGb);
        final ActorGestureListener listener = new ActorGestureListener() {
            public void tap(InputEvent event, float x, float y, int count, int button) {
                if(!flyingGb.is_checked()) {
                    textBox.setText(textBox.getText() + flyingGb.getText());
                    flyingGb.check();
                }
            }
        };
        flyingGb.addListener(listener);


        MoveToAction action = new MoveToAction();

        action.setX(flyingGb.getX());
        int n_squares = 0;
        for(int i = 0; i < gbs.get(col).size(); i++) {
            if (gbs.get(col).get(i) != null) {
                n_squares++;
            }
        }
        action.setY(Gdx.graphics.getHeight() - square * (n_squares + 1));
        action.setDuration(Math.abs((Gdx.graphics.getHeight() - square * (n_squares + 1)) / v));



        Action generateNext = new Action() {
            public boolean act( float delta ) {
                flyingGb.flies = false;
                if(gbs.get(col).size() > columnHeight)
                    gameOver();
                else
                    generateGameButton();
                return true;
            }
        };

        Action actions = new SequenceAction(action, generateNext);

        flyingGb.addAction(actions);

        addActor(flyingGb);
    }

    public void gameOver() {
        System.out.println("GAME OVER your score is: " + score);

        Rect rect = new Rect();
        Color rectColor = Color.BLACK;
        rectColor.a = 0.9f;
        rect.setBackgroundColor(rectColor);
        rect.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        addActor(rect);


        final VerticalLayout vL = new VerticalLayout();
        vL.setAlign(VerticalLayout.Align.Center);
        vL.setPosition(Gdx.graphics.getWidth() / 2, vL.getY());
        Text gamoverText = new Text("ИГРА ОКОНЧЕНА");
        vL.addActor(gamoverText);
        Text yoScore = new Text("Ваш счет: "+score);
        vL.addActor(yoScore);
        final FieldText name = new FieldText();
        if(checkRecord()) {
            Text insertName = new Text("Новый рекорд! Введите имя:");
            vL.addActor(insertName);
            vL.addActor(name);
        }

        Button restart = new Button("ЗАНОВО");
        restart.setSize(buttonWidth, buttonHeight);
        restart.addListener(new ActorGestureListener() {
            public void tap(InputEvent event, float x, float y, int count, int button) {
                if(checkRecord()) {
                    addRecord(name.getText());
                }
                LetterCrush.setCurrentScreen(new GameScreen());
            }
        });
        vL.addActor(restart);

        Button goMenu = new Button("В МЕНЮ");
        goMenu.setSize(buttonWidth, buttonHeight);
        goMenu.addListener(new ActorGestureListener() {
            public void tap(InputEvent event, float x, float y, int count, int button) {
                if(checkRecord()) {
                    addRecord(name.getText());
                }
                LetterCrush.setCurrentScreen(new MenuScreen());
            }
        });
        vL.addActor(goMenu);


        addActor(vL);
    }

    public void compress() {
        for(final ArrayList<GameButton> list : gbs) {
            for(int i = 1; i < list.size(); i++) {
                if(list.get(i)!=null && !(list.get(i)).is_flying()) {
                    int n_nulls = 0;
                    for (int j = 0; j < i; j++) {
                        if(list.get(j)==null) {
                            n_nulls++;
                        }
                    }
                    list.get(i).setY(list.get(i).getY() + n_nulls * square);
                }
                else {
                    if(list.get(i)!=null && list.get(i).is_flying()) {
                        list.get(i).clearActions();
                        MoveToAction action = new MoveToAction();

                        action.setX(list.get(i).getX());
                        int n_squares = 0;
                        for(int k = 0; k < gbs.get(gbs.indexOf(list)).size(); k++) {
                            if (gbs.get(gbs.indexOf(list)).get(k) != null) {
                                n_squares++;
                            }
                        }
                        action.setY(Gdx.graphics.getHeight() - square * (n_squares + 1));

                        action.setDuration(Math.abs(Gdx.graphics.getHeight() - list.get(i).getY() - square * (n_squares + 1)) / v);


                        final GameButton gb = list.get(i);
                        Action generateNext = new Action() {
                            public boolean act( float delta ) {
                                gb.flies = false;
                                if(gbs.get(gbs.indexOf(list)).size() > columnHeight)
                                    gameOver();
                                else
                                    generateGameButton();
                                return true;
                            }
                        };
                        Action actions = new SequenceAction(action, generateNext);
                        list.get(i).addAction(actions);
                    }
                }
            }
            list.removeAll(Collections.singleton(null));
        }
    }

    public boolean checkRecord() {
        if(score > topResults.get(9)) return true;
        else return false;
    }

    public void addRecord(String name) {
        topResults.add(score);
        Collections.sort(topResults, Collections.<Integer>reverseOrder());
        leaders.add(topResults.indexOf(score), name);
        topResults.subList(10, topResults.size());
        leaders.subList(10, leaders.size());

        String[] ss = new String[10];
        for(int i = 0; i < ss.length; i++) {
            ss[i] = leaders.get(i) + "@@@@@" + topResults.get(i);
        }

        AppUtils.files.writeFile(new FileHandle("leaderboard"), ss);

    }
}
