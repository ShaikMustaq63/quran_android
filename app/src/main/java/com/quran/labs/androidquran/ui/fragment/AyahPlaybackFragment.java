package com.quran.labs.androidquran.ui.fragment;


import com.quran.labs.androidquran.R;
import com.quran.labs.androidquran.data.QuranInfo;
import com.quran.labs.androidquran.data.SuraAyah;
import com.quran.labs.androidquran.service.util.AudioRequest;
import com.quran.labs.androidquran.ui.PagerActivity;
import com.quran.labs.androidquran.util.QuranUtils;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

public class AyahPlaybackFragment extends AyahActionFragment {
  private static final int REPEAT_MAX = 3;

  private SuraAyah mDecidedStart;
  private SuraAyah mDecidedEnd;
  private boolean mShouldEnforce;
  private int mRangeRepeatCount;
  private int mVerseRepeatCount;

  private Button mApplyButton;
  private Spinner mStartSuraSpinner;
  private Spinner mStartAyahSpinner;
  private Spinner mEndingSuraSpinner;
  private Spinner mEndingAyahSpinner;
  private Spinner mRepeatVerseSpinner;
  private Spinner mRepeatRangeSpinner;
  private CheckBox mRestrictToRange;
  private ArrayAdapter<CharSequence> mStartAyahAdapter;
  private ArrayAdapter<CharSequence> mEndingAyahAdapter;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    final View view = inflater.inflate(
        R.layout.audio_panel, container, false);
    view.setOnClickListener(mOnClickListener);

    mStartSuraSpinner = (Spinner) view.findViewById(R.id.start_sura_spinner);
    mStartAyahSpinner = (Spinner) view.findViewById(R.id.start_ayah_spinner);
    mEndingSuraSpinner = (Spinner) view.findViewById(R.id.end_sura_spinner);
    mEndingAyahSpinner = (Spinner) view.findViewById(R.id.end_ayah_spinner);
    mRepeatVerseSpinner = (Spinner) view
        .findViewById(R.id.repeat_verse_spinner);
    mRepeatRangeSpinner = (Spinner) view
        .findViewById(R.id.repeat_range_spinner);
    mRestrictToRange = (CheckBox) view.findViewById(R.id.restrict_to_range);
    mApplyButton = (Button) view.findViewById(R.id.apply);
    mApplyButton.setOnClickListener(mOnClickListener);

    final Context context = getActivity();
    mStartAyahAdapter = initializeAyahSpinner(context, mStartAyahSpinner);
    mEndingAyahAdapter = initializeAyahSpinner(context, mEndingAyahSpinner);
    initializeSuraSpinner(context, mStartSuraSpinner, mStartAyahAdapter);
    initializeSuraSpinner(context, mEndingSuraSpinner, mEndingAyahAdapter);

    final String[] repeatOptions = context.getResources()
        .getStringArray(R.array.repeatValues);
    final ArrayAdapter<CharSequence> rangeAdapter =
        new ArrayAdapter<CharSequence>(context,
            android.R.layout.simple_spinner_item, repeatOptions);
    rangeAdapter.setDropDownViewResource(
        android.R.layout.simple_spinner_dropdown_item);
    mRepeatRangeSpinner.setAdapter(rangeAdapter);
    final ArrayAdapter<CharSequence> verseAdapter =
        new ArrayAdapter<CharSequence>(context,
            android.R.layout.simple_spinner_item, repeatOptions);
    verseAdapter.setDropDownViewResource(
        android.R.layout.simple_spinner_dropdown_item);
    mRepeatVerseSpinner.setAdapter(verseAdapter);
    return view;
  }

  private View.OnClickListener mOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      switch (v.getId()) {
        case R.id.apply: {
          apply();
          break;
        }
      }
    }
  };

  private void apply() {
    final Context context = getActivity();
    if (context instanceof PagerActivity) {
      final SuraAyah currentStart = new SuraAyah(
          mStartSuraSpinner.getSelectedItemPosition() + 1,
          mStartAyahSpinner.getSelectedItemPosition() + 1);
      final SuraAyah currentEnding = new SuraAyah(
          mEndingSuraSpinner.getSelectedItemPosition() + 1,
          mEndingAyahSpinner.getSelectedItemPosition() + 1);
      final int page = QuranInfo.getPageFromSuraAyah(
          currentStart.sura, currentStart.ayah);
      final int verseRepeat = positionToRepeat(
          mRepeatVerseSpinner.getSelectedItemPosition());
      final int rangeRepeat = positionToRepeat(
          mRepeatRangeSpinner.getSelectedItemPosition());
      final boolean enforceRange = mRestrictToRange.isChecked();

      boolean updatedRange = false;
      final PagerActivity pagerActivity = (PagerActivity) context;
      if (!currentStart.equals(mDecidedStart) ||
          !currentEnding.equals(mDecidedEnd)) {
        // different range or not playing, so make a new request
        updatedRange = true;
        pagerActivity.playFromAyah(currentStart.toQuranAyah(),
            currentEnding.toQuranAyah(), page, verseRepeat, rangeRepeat,
            enforceRange, true);
      } else if (mShouldEnforce != enforceRange ||
          mRangeRepeatCount != rangeRepeat ||
          mVerseRepeatCount != verseRepeat) {
        // can just update repeat settings
        pagerActivity.updatePlayOptions(
            rangeRepeat, verseRepeat, enforceRange);
      }
      pagerActivity.endAyahMode();
      if (updatedRange) {
        pagerActivity.toggleActionBarVisibility(true);
      }
    }
  }

  private void initializeSuraSpinner(final Context context, Spinner spinner,
      final ArrayAdapter<CharSequence> ayahAdapter) {
    String[] suras = context.getResources().
        getStringArray(R.array.sura_names);
    for (int i=0; i<suras.length; i++){
      suras[i] = QuranUtils.getLocalizedNumber(context, (i + 1)) +
          ". " + suras[i];
    }
    ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
        context, android.R.layout.simple_spinner_item, suras);
    adapter.setDropDownViewResource(
        android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);

    spinner.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view,
              int position, long rowId) {
            int sura = position + 1;
            int ayahCount = QuranInfo.getNumAyahs(sura);
            CharSequence[] ayahs = new String[ayahCount];
            for (int i = 0; i < ayahCount; i++){
              ayahs[i] = QuranUtils.getLocalizedNumber(context, (i + 1));
            }
            ayahAdapter.clear();

            for (int i=0; i<ayahCount; i++){
              ayahAdapter.add(ayahs[i]);
            }
          }

          @Override
          public void onNothingSelected(AdapterView<?> arg0) {
          }
        });
  }

  private ArrayAdapter<CharSequence> initializeAyahSpinner(
      Context context, Spinner spinner) {
    final ArrayAdapter<CharSequence> ayahAdapter =
        new ArrayAdapter<CharSequence>(context,
            android.R.layout.simple_spinner_item);
    ayahAdapter.setDropDownViewResource(
        android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(ayahAdapter);
    return ayahAdapter;
  }

  private void updateAyahSpinner(Spinner spinner,
                                 ArrayAdapter<CharSequence> adapter,
                                 int maxAyah, int currentAyah) {
    final Context context = getActivity();
    if (context != null) {
      CharSequence[] ayahs = new String[maxAyah];
      for (int i = 0; i < maxAyah; i++) {
        ayahs[i] = QuranUtils.getLocalizedNumber(context, (i + 1));
      }
      adapter.clear();

      for (int i = 0; i < maxAyah; i++) {
        adapter.add(ayahs[i]);
      }
      spinner.setSelection(currentAyah - 1);
    }
  }

  private int repeatToPosition(int repeat) {
    if (repeat == -1) {
      return REPEAT_MAX;
    } else {
      return repeat;
    }
  }

  private int positionToRepeat(int position) {
    if (position > REPEAT_MAX) {
      return -1;
    } else {
      return position;
    }
  }

  @Override
  protected void refreshView() {
    final Context context = getActivity();
    if (context instanceof PagerActivity) {
      final AudioRequest lastRequest =
          ((PagerActivity) context).getLastAudioRequest();
      final SuraAyah start;
      final SuraAyah ending;
      if (lastRequest != null) {
        start = lastRequest.getRangeStart();
        ending = lastRequest.getRangeEnd();
        mVerseRepeatCount = lastRequest.getRepeatInfo().getRepeatCount();
        mRangeRepeatCount = lastRequest.getRangeRepeatCount();
        mShouldEnforce = lastRequest.shouldEnforceBounds();
        mDecidedStart = start;
        mDecidedEnd = ending;
        mApplyButton.setText(R.string.play_apply);
      } else {
        start = mStart;
        if (mStart.equals(mEnd)) {
          ending = new SuraAyah(start.sura,
              QuranInfo.getNumAyahs(start.sura));
          mShouldEnforce = false;
        } else {
          ending = mEnd;
          mShouldEnforce = true;
        }
        mRangeRepeatCount = 0;
        mVerseRepeatCount = 0;
        mDecidedStart = null;
        mDecidedEnd = null;
        mApplyButton.setText(R.string.play_apply_and_play);
      }

      final int maxAyat = QuranInfo.getNumAyahs(start.sura);
      updateAyahSpinner(mStartAyahSpinner, mStartAyahAdapter,
          maxAyat, start.ayah);
      final int endAyat = (ending.sura == start.sura) ? maxAyat :
          QuranInfo.getNumAyahs(ending.sura);
      updateAyahSpinner(mEndingAyahSpinner, mEndingAyahAdapter,
          endAyat, ending.ayah);
      mStartSuraSpinner.setSelection(start.sura - 1);
      mEndingSuraSpinner.setSelection(ending.sura - 1);
      mRepeatRangeSpinner.setSelection(repeatToPosition(mRangeRepeatCount));
      mRepeatVerseSpinner.setSelection(repeatToPosition(mVerseRepeatCount));
      mRestrictToRange.setChecked(mShouldEnforce);
    }
  }
}
