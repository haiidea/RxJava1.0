package haiidea.com.rxjava10;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by Administrator on 2018/7/23.
 */

public class flatmap {
    public static void just(){
        Student[] students = new Student[10];
        final List<Student> studentList = null;
        Subscriber<Course> subscriber = new Subscriber<Course>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Course course) {
                Log.d("", course.getName());
            }
        };
        Observable.from(students)
                .flatMap(new Func1<Student, Observable<Course>>() {
                    @Override
                    public Observable<Course> call(Student student) {
                        return Observable.from(student.getCourses());
                    }
                })
                .subscribe(subscriber);
        //或者
//        Observable.from(studentList)
        Observable.create(new Observable.OnSubscribe<Student>() {
            @Override
            public void call(Subscriber<? super Student> subscriber) {
                for (Student student: studentList ) {
                    subscriber.onNext(student);

                }
            }
        }).flatMap(new Func1<Student, Observable<Course>>() {
            @Override
            public Observable<Course> call(Student student) {
                return Observable.from(student.getCourses());
            }
        }) .subscribe(subscriber);

    }
}
class Course{
    public String getName(){
        return "";
    }
}
class Student{
    public List<Course> getCourses(){
        return new ArrayList<>();
    }
}
