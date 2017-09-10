package medvedev.lettercrush.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.idp.engine.App;
import com.idp.engine.ui.graphics.actors.Text;
import com.idp.engine.ui.graphics.base.Widget;

public class GameButton extends Widget {


    private Text data;
    private Boolean check = false;
    public Boolean flies;



    public GameButton(String t) {
        this.data = new Text(t, App.getResources().getLabelStyle("button"));
        this.data.setAlignment(Align.center);
        data.layout();
        this.addActor(data);
        init();
    }

    public Boolean is_flying() {
        return flies;
    }


    public void setText(String text) {
        this.data.setText(text);
        this.data.layout();
    }

    public String getText() {
        return data.getText();
    }

    public void setTextStyle(Label.LabelStyle style) {
        data.setStyle(style);
        data.layout();
        //setHeight(data.getHeight() * 2);
    }

    public void setTextColor(Color c) {
        data.getStyle().fontColor = c;
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        data.setSize(this.getWidth(), this.getHeight());
    }

    public void uncheck() {
        if(check) {
            Color b = getBackgroundColor();
            Color c = new Color(b.r * 1.25f, b.g * 1.25f, b.b * 1.25f, 1);
            setBackgroundColor(c);
            check = false;
        }
    }

    public boolean is_checked() {
        return check;
    }

    public void check() {
        Color b = getBackgroundColor();
        Color c = new Color(b.r * .8f, b.g * .8f, b.b * .8f, 1);
        setBackgroundColor(c);
        check = true;
    }



    @Override
    protected void init() {
        setHeight(App.dp2px(36));
        setWidth(App.dp2px(96));
        setBackgroundColor(App.Colors.MAIN);

        Color b = getBackgroundColor();
        float coef = 0.299f * b.r + 0.587f * b.g + 0.114f * b.b;
        if (coef > 0.5) {
            setTextColor(Color.BLACK);
        } else {
            setTextColor(Color.WHITE);
        }

        addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return super.touchDown(event, x, y, pointer, button);
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }



}