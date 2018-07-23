package haiidea.com.rxjava10;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Action4;
import rx.schedulers.Schedulers;

/**
 * RX线程默认是哪个线程创建在哪个线程执行在哪个线程订阅。要实现异步需要Scheduler(调度器)
 * 四个基本概念
 * Observable -- 被观察
 * Observer -- 观察者
 * subscribe -- 订阅，事件
 * Observable 和 Observer 通过 subscribe() 方法实现订阅关系
 * Observable 可以在需要的时候发出事件来通知 Observer，只有在被订阅以后才会执行call
 *
 * 创建了 Observable 和 Observer 之后，再用 subscribe() 方法将它们联结起来，整条链子就可以工作了
 *
 * Subscriber是实现了Observer的抽象类，进行了扩展,其实在subscribe中也是先创建一个Subscriber。
 * 新增了onStart，在事件发送之前被调用，做准备工作(例如数据清除)但不能做UI的操作(例如进度条)，如果一定要做需要doOnSubscribe
 *
 * unsubscribe 取消订阅,之后Subscriber将不再接收消息，调用前可用isUnsubscribed判断一下是否已经取消过，一般在onPause,onStop中调用
 *
 * 除了Observer和Subscriber，观察者还有不完全定义Action0,Action1..2..3
 *
 * 五种调度器
 * Schedulers.immediate(): 直接在当前线程运行，相当于不指定线程。这是默认的 Scheduler。
 * Schedulers.newThread(): 总是启用新线程，并在新线程执行操作。
 * Schedulers.io(): I/O 操作（读写文件、读写数据库、网络信息交互等）所使用的 Scheduler。
 *      行为模式和 newThread() 差不多，区别在于 io() 的内部实现是是用一个无数量上限的线程池，可以重用空闲的线程，
 *      因此多数情况下 io() 比 newThread() 更有效率。
 *      不要把计算工作放在 io() 中，可以避免创建不必要的线程。
 * Schedulers.computation(): 计算所使用的 Scheduler，CPU 密集型计算。
 *      这个 Scheduler 使用的固定的线程池，大小为 CPU 核数。不要把 I/O 操作放在 computation() 中，否则 I/O 操作的等待时间会浪费 CPU。
 * AndroidSchedulers.mainThread()，它指定的操作将在 Android 主线程运行。
 *
 * subscribeOn() 和 observeOn() 两个方法来对线程进行控制了。
 * subscribeOn(): 指定 subscribe() 所发生的线程，即 Observable.OnSubscribe 被激活时所处的线程。或者叫做事件产生的线程。
 * observeOn(): 指定 Subscriber 所运行在的线程。或者叫做事件消费的线程。
 *
 * 变换 改变事件序列例如将文件路径字符串变成bitmap
 */
public class MainActivity extends AppCompatActivity {
    File[] folders;
    String tag="tag";

    //定义一个被观察者
    Observable observable = Observable.create(new Observable.OnSubscribe<String>() {
        @Override
        public void call(Subscriber<? super String> subscriber) {
            System.out.println("创建一个被观察者");
            subscriber.onNext("Hello");
            subscriber.onNext("Hi");
            subscriber.onNext("Aloha");
            subscriber.onCompleted();
        }
    });
    // 与上面的代码效果是一样的
    Observable observable2 = Observable.just("Hello", "Hi", "Aloha");
    String[] words = {"Hello", "Hi", "Aloha"};
    // 与上面的代码效果是一样的
    Observable observable3 = Observable.from(words);

    // 定义一个观察者
    Observer<String> observer = new Observer<String>() {
        @Override
        public void onNext(String s) {
            Log.d(tag, "Item: " + s);
        }

        @Override
        public void onCompleted() {
            Log.d(tag, "Completed!");
        }

        @Override
        public void onError(Throwable e) {
            Log.d(tag, "Error!");
        }
    };
    //Subscriber是实现了Observer的抽象类，进行了扩展
    Subscriber<String> subscriber = new Subscriber<String>() {

        @Override
        public void onNext(String s) {
            Log.d(tag, "Item: " + s);
            System.out.println("创建一个被观察者"+s);
        }

        @Override
        public void onCompleted() {
            Log.d(tag, "Completed!");
        }

        @Override
        public void onError(Throwable e) {
            Log.d(tag, "Error!");
        }
    };
    //不完全定义的订阅者
    Action1<String> onNextAction = new Action1<String>() {
        // onNext()
        @Override
        public void call(String s) {
            Log.d(tag, s);
        }
    };
    Action1<Throwable> onErrorAction = new Action1<Throwable>() {
        // onError()
        @Override
        public void call(Throwable throwable) {
            // Error handling
        }
    };
    Action0 onCompletedAction = new Action0() {
        // onCompleted()
        @Override
        public void call() {
            Log.d(tag, "completed");
        }
    };
    Action4 action4 = new Action4() {
        @Override
        public void call(Object o, Object o2, Object o3, Object o4) {

        }
    };
//    Observable.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        observable.subscribeOn(Schedulers.io()) // 事件在io线程中执行相当于异步
                .observeOn(AndroidSchedulers.mainThread()) //相应在主线程执行可以操作UI
                .subscribe(subscriber);

        observable.subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
//                        progressBar.setVisibility(View.VISIBLE); // 需要在主线程执行
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread()) // 指定主线程
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);

//        Observable.from(folders)
//                .flatMap(new Func1<File, Observable<File>>() {
//                    @Override
//                    public Observable<File> call(File file) {
//                        if (file==null){
//                            return null;
//                        }
//                        return Observable.from(file.listFiles());
//                    }
//                })
//                .filter(new Func1<File, Boolean>() {
//                    @Override
//                    public Boolean call(File file) {
//                        if (file==null){
//                            return false;
//                        }
//                        return file.getName().endsWith(".png");
//                    }
//                })
//                .map(new Func1<File, Bitmap>() {
//                    @Override
//                    public Bitmap call(File file) {
//                        return getBitmapFromFile(file);
//                    }
//                })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Action1<Bitmap>() {
//                    @Override
//                    public void call(Bitmap bitmap) {
////                        imageCollectorView.addImage(bitmap);
//                    }
//                });

    }
    private Bitmap getBitmapFromFile(File file){
        Bitmap b=null;
        return b;
    }
}
