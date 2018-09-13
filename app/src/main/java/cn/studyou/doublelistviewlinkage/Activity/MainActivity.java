package cn.studyou.doublelistviewlinkage.Activity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.studyou.doublelistviewlinkage.Adapter.LeftListAdapter;
import cn.studyou.doublelistviewlinkage.Adapter.MainSectionedAdapter;
import cn.studyou.doublelistviewlinkage.Adapter.RecycleAdapter;
import cn.studyou.doublelistviewlinkage.R;
import cn.studyou.doublelistviewlinkage.View.PinnedHeaderListView;

public class MainActivity extends AppCompatActivity {

    public static final int SHOW_RESPONSE = 0;

    //数据读取定时器
    private Timer readDataTimer;

    //数据读取时间显示视图
    @Bind(R.id.snatchTime)
    TextView readDataTime;

    //滚动视图
    @Bind(R.id.warningView)
    TextView warningView;

    //报警数据
    String warningData="";

    //左列表
    @Bind(R.id.left_listview)
    ListView leftListview;

    @Bind(R.id.pinnedListView)
    PinnedHeaderListView pinnedListView;



    //是否在滚动
    private boolean isScroll = true;
    //左列数据适配器
    private LeftListAdapter adapter;
    //?
    MainSectionedAdapter sectionedAdapter;

    //左列数据
    private String[] leftStr = new String[]{ "网络设备0","网络设备1", "网络设备2", "网络设备3"};
    //？
    private boolean[] flagArray = {true, false, false, false};
    //右列数据
    private String[][] rightStr = new String[][]
            {
            {"内存容量：99%", "CPU占用99%", "硬盘占用99%","硬盘占用99%"},
            {"网络流量：XX", "最大包：XXX", "日攻击次数："},
            {"内存容量：99%", "CPU占用99%", "硬盘占用99%"},
            {"网络流量：XX", "最大包：XXX", "日攻击次数："}
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //去除App的标题栏
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        pinnedListView = (PinnedHeaderListView) findViewById(R.id.pinnedListView);
        this.setData();;
        leftListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                isScroll = false;

                for (int i = 0; i < leftStr.length; i++) {
                    if (i == position) {
                        flagArray[i] = true;
                    } else {
                        flagArray[i] = false;
                    }
                }
                adapter.notifyDataSetChanged();
                int rightSection = 0;
                for (int i = 0; i < position; i++) {
                    rightSection += sectionedAdapter.getCountForSection(i) + 1;
                }
                pinnedListView.setSelection(rightSection);

            }

        });

        pinnedListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView arg0, int scrollState) {
                switch (scrollState) {
                    // 当不滚动时
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        // 判断滚动到底部
                        if (pinnedListView.getLastVisiblePosition() == (pinnedListView.getCount() - 1)) {
                            leftListview.setSelection(ListView.FOCUS_DOWN);
                        }

                        // 判断滚动到顶部
                        if (pinnedListView.getFirstVisiblePosition() == 0) {
                            leftListview.setSelection(0);
                        }

                        break;
                }
            }

            int y = 0;
            int x = 0;
            int z = 0;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (isScroll) {
                    for (int i = 0; i < rightStr.length; i++) {
                        if (i == sectionedAdapter.getSectionForPosition(pinnedListView.getFirstVisiblePosition())) {
                            flagArray[i] = true;
                            x = i;
                        } else {
                            flagArray[i] = false;
                        }
                    }
                    if (x != y) {
                        adapter.notifyDataSetChanged();
                        y = x;
                        if (y == leftListview.getLastVisiblePosition()) {
//                            z = z + 3;
                            leftListview.setSelection(z);
                        }
                        if (x == leftListview.getFirstVisiblePosition()) {
//                            z = z - 1;
                            leftListview.setSelection(z);
                        }
                        if (firstVisibleItem + visibleItemCount == totalItemCount - 1) {
                            leftListview.setSelection(ListView.FOCUS_DOWN);
                        }
                    }
                } else {
                    isScroll = true;
                }
            }
        });

        //定义layoutManager
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);

        //数据读取定时器
        readDataTimer = new Timer();
        readDataTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getData() ;
            }
        }, 0, 10*1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    public static boolean isSlideToBottom(RecyclerView recyclerView) {
        if (recyclerView == null) return false;
        if (recyclerView.computeHorizontalScrollExtent() + recyclerView.computeHorizontalScrollOffset() >= recyclerView.computeHorizontalScrollRange())
            return true;
        else
            return false;
    }

    //方法：发送网络请求，获取百度首页的数据。在里面开启线程
    private void getData() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                //在子线程中将Message对象发出去
                Message message = new Message();
                //用HttpClient发送请求，分为五步
                //第一步：创建HttpClient对象
                HttpClient httpCient = new DefaultHttpClient();
                //第二步：创建代表请求的对象,参数是访问的服务器地址
                HttpGet httpGet = new HttpGet("http://192.168.8.102:8082/EchoServlet");
                try {
                    //第三步：执行请求，获取服务器发还的相应对象
                    HttpResponse httpResponse = httpCient.execute(httpGet);
                    //第四步：检查相应的状态是否正常：检查状态码的值是200表示正常
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        //第五步：从相应对象当中取出数据，放到entity当中
                        HttpEntity entity = httpResponse.getEntity();
                        String response = EntityUtils.toString(entity,"utf-8");//将entity当中的数据转换为字符串
                        message.what = SHOW_RESPONSE;
                        message.obj = response.toString();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                handler.sendMessage(message);
            }
        }).start();//这个start()方法不要忘记了

    }

    //新建Handler的对象，在这里接收Message，然后更新TextView控件的内容，并显示报警数据
    @SuppressLint("HandlerLeak")
    private MyHandler handler = new MyHandler(this) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_RESPONSE:
                    try {
                    //接收数据
                    System.out.println(msg.obj);
                    JSONObject response = new JSONObject((String) msg.obj);
                     //接收的数据格式转换
                     convertJsonToArray(response);
                        //设置数据获取时间
                      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
                      Date date = new Date(System.currentTimeMillis());
                      readDataTime.setText("数据新获时间："+simpleDateFormat.format(date));
                      //报警数据,50个字节以上充满行，则滚动
                      //warningView.setText(warningData);
                        //通知adapter数据更新
                      this.a.setData();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // dataContent.setText(response);
                    break;
                default:
                    break;
            }
        }

    };

    /*
    将接收的json格式 主机名：[{},{}...] 数据转为数组
     */
    private void convertJsonToArray( JSONObject response) throws JSONException {
        LinkedList<String> left=new LinkedList<>();
        LinkedList flag=new LinkedList();
        LinkedList<JSONArray> right = new LinkedList<>();

        Iterator<String> sIterator = response.keys();
        while(sIterator.hasNext()){
            // 获得key
            String key = sIterator.next();
            left.add(key);
            flag.add(false);
            // 根据key获得value, value也可以是JSONObject,JSONArray,使用对应的参数接收即可
            JSONArray value = response.getJSONArray(key);
            right.add(value);
        }
        //左边数组
        this.leftStr = new String[left.size()];
        left.toArray(this.leftStr);
        //标识位
        this.flagArray = new boolean[left.size()];
        for(boolean a:flagArray){
            a=false;
        }
        flagArray[0]=true;

        //右边数组
        this.rightStr = new String[right.size()][];
        for(int i=0 ;i<right.size();i++){
            JSONArray temp=((JSONArray)right.get(i));
            this.rightStr[i]=new String[temp.length()];
            for(int j=0;j<temp.length();j++){
                this.rightStr[i][j] =temp.get(j).toString();
            }
        }
    }
    /*
    数组数据变化后，重新构造适配器
     */
    public  void setData(){
        sectionedAdapter = new MainSectionedAdapter(this, leftStr, rightStr);
        pinnedListView.setAdapter(sectionedAdapter);
        adapter = new LeftListAdapter(this, leftStr, flagArray);
        leftListview.setAdapter(adapter);
    }

    class MyHandler extends Handler{
        public MainActivity a;
        public MyHandler(MainActivity a){
            super();
            this.a =a;
        }

    }
}

