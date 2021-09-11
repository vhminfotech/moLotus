package com.sms.moLotus.common.androidxcompat;

import android.view.View;
import androidx.drawerlayout.widget.DrawerLayout;
import com.jakewharton.rxbinding2.InitialValueObservable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

import static com.jakewharton.rxbinding2.internal.Preconditions.checkMainThread;

final class DrawerLayoutDrawerOpenedObservable extends InitialValueObservable<Boolean> {
  private final DrawerLayout view;
  private final int gravity;

  DrawerLayoutDrawerOpenedObservable(DrawerLayout view, int gravity) {
    this.view = view;
    this.gravity = gravity;
  }

  @Override protected void subscribeListener(Observer<? super Boolean> observer) {
    if (!checkMainThread(observer)) {
      return;
    }
    Listener listener = new Listener(view, gravity, observer);
    observer.onSubscribe(listener);
    view.addDrawerListener(listener);
  }

  @Override protected Boolean getInitialValue() {
    return view.isDrawerOpen(gravity);
  }

  static final class Listener extends MainThreadDisposable implements DrawerLayout.DrawerListener {
    private final DrawerLayout view;
    private final int gravity;
    private final Observer<? super Boolean> observer;

    Listener(DrawerLayout view, int gravity, Observer<? super Boolean> observer) {
      this.view = view;
      this.gravity = gravity;
      this.observer = observer;
    }

    @Override public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override public void onDrawerOpened(View drawerView) {
      if (!isDisposed()) {
        int drawerGravity = ((DrawerLayout.LayoutParams) drawerView.getLayoutParams()).gravity;
        if (drawerGravity == gravity) {
          observer.onNext(true);
        }
      }
    }

    @Override public void onDrawerClosed(View drawerView) {
      if (!isDisposed()) {
        int drawerGravity = ((DrawerLayout.LayoutParams) drawerView.getLayoutParams()).gravity;
        if (drawerGravity == gravity) {
          observer.onNext(false);
        }
      }
    }

    @Override public void onDrawerStateChanged(int newState) {

    }

    @Override protected void onDispose() {
      view.removeDrawerListener(this);
    }
  }
}