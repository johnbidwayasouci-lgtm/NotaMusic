# QA_MATRIX

Date: 2026-08-31

## Scope

This audit compares the independent NotaMusic implementation with observable evidence from the reference APK `ensemble-composer-1-3-0.apk`. The APK was inspected at the archive/dex/string level. The reference exposes activities/classes including `MusicComposerActivity`, `CreateActivity`, `OpenActivity`, `EditActivity`, `SettingsActivity`, `ScoreMidi`, `ScoreXml`, `StaffConfigItemView`, and resource/string families for create, settings, staff configuration, sharing, dialogs and errors.

Status meanings:
- REFERENCE: observable in the APK and behavior/contract is established.
- IMPLEMENTED: present in NotaMusic with a corresponding implementation that can be inspected.
- PARTIAL: some of the function exists but important behavior or fidelity is missing.
- MISSING: no credible implementation found.
- UNVERIFIED: implementation/evidence exists but cannot be honestly validated end-to-end in this environment.

## Matrix

| # | Function | Reference | NotaMusic | Status | Main gap / evidence |
|---:|---|---|---|---|---|
| 1 | Home | Home activity/resources/buttons | Android UI exists | PARTIAL | End-to-end device behavior not verified. |
| 2 | Navigation | Create/Open/Settings/Edit activities | Modular destinations exist | PARTIAL | Full back-stack audit unverified. |
| 3 | Score creation | CreateActivity + create_* resources | Creation flow exists | PARTIAL | Full transaction and persistence flow unverified. |
| 4 | Metadata | create_title/subtitle/composer/rights/encoder/source | Metadata model/UI exists | PARTIAL | Full round-trip unverified. |
| 5 | Instruments | instrument group/item/child resources | Instrument model/catalog exists | PARTIAL | Reference catalog parity not established. |
| 6 | Clefs | key/clef selection resources | Clef model/UI exists | IMPLEMENTED | Runtime/device validation unverified. |
| 7 | Time signatures | 2/2, 2/4, 3/4, 4/4, 3/8, 5/4, 6/8, 7/8, 9/8, 11/8, 12/8 and 1/4 resources | Extensible TimeSignature model | PARTIAL | Exact UI/reference behavior not fully audited. |
| 8 | Tempo | create_tempo_* / modify_tempo_* | Tempo model/UI exists | PARTIAL | Playback linkage unverified. |
| 9 | Staff configuration | up/down/delete/mute/add/clef resources | Staff configuration exists | PARTIAL | Complete interaction and persistence unverified. |
| 10 | Editor | EditActivity + editor resources | Score editor exists | PARTIAL | Rendering and tactile interaction remain incomplete. |
| 11 | Tool groups | function/voice/note/add-one/ornament/dynamics/measure evidence | Tool groups exist | PARTIAL | Exact reference behavior and all actions not validated. |
| 12 | Notes | note resources/classes | Note model/insertion exists | IMPLEMENTED | Visual engraving incomplete. |
| 13 | Rests | rest resources/classes | Rest model/insertion exists | IMPLEMENTED | Visual engraving incomplete. |
| 14 | Accidentals | sharp/flat resources | Accidental model exists | IMPLEMENTED | Full rendering/round-trip unverified. |
| 15 | Dots | note-duration controls | Duration dots exist | IMPLEMENTED | Device interaction unverified. |
| 16 | Tuplets | dialog_tuplet and tuplets/toast strings | Tuplet model exists | PARTIAL | Full editor interaction/rendering incomplete. |
| 17 | Ornaments | trill/mordent/turn/fermata/tremolo/grace evidence | Ornament model exists | PARTIAL | Rendering and complete editing not validated. |
| 18 | Articulations | staccato/accent evidence | Model exists | PARTIAL | Full rendering not validated. |
| 19 | Dynamics | ppp..fff strings/resources | Dynamic model exists | PARTIAL | Rendering/playback mapping not validated. |
| 20 | Wedges | dialog_wedge_* / toast_wedge_* | Wedge model exists | PARTIAL | Interaction/rendering incomplete. |
| 21 | Ties | dialog/toast tie evidence | Tie fields/model exist | PARTIAL | Validity and playback not fully validated. |
| 22 | Slurs | dialog_slur_* evidence | Slur model exists | PARTIAL | Rendering and validation incomplete. |
| 23 | Repeats | barline/repeat evidence | Repeat fields/model exist | PARTIAL | Playback semantics unverified. |
| 24 | Measures | measure model/edit controls | Measure model exists | IMPLEMENTED | Complex editing regression suite incomplete. |
| 25 | Voices | dialog_voice_ignore + voice model | Voice model exists | PARTIAL | Complete UI/renderer isolation unverified. |
| 26 | Playback | EditActivity PlayThread / ScoreMidi | PlaybackController exists | UNVERIFIED | Real Android audio output not executed here. |
| 27 | Mute | staffconfig_mute | Staff mute field exists | PARTIAL | Audio effect unverified. |
| 28 | Save | dialog_save/want_save/toast save evidence | FileScoreRepository exists | PARTIAL | Android lifecycle integration unverified. |
| 29 | Open | OpenActivity + file_row resources | Repository/Open UI exists | PARTIAL | Real close/reopen scenario unverified. |
| 30 | Delete | dialog_delete_file/toast_cannot_delete_file | Delete operation exists | PARTIAL | UI/error behavior unverified. |
| 31 | MusicXML | ScoreXml + XML resource/signature evidence | MusicXmlCodec exists | PARTIAL | Current codec does not yet preserve the full model. |
| 32 | MIDI | ScoreMidi | MIDI package/codec exists | PARTIAL | End-to-end binary/playback validation unavailable. |
| 33 | Image generation | dialog_generate_image/shareimage_* | Rendering foundation exists | PARTIAL | High-resolution independent output not validated. |
| 34 | Sharing | shareimage_mail/fb/sd + share dialogs | Android sharing foundation exists | PARTIAL | URI/share target matrix unverified. |
| 35 | Settings | settings_* families | Settings repository exists | IMPLEMENTED | Device persistence/lifecycle unverified. |
| 36 | Help/tutorial | create_firsttime_guide/editview_firsttime/scoreview_firsttime/settings_tutorial | Tutorial resources/UI foundation | PARTIAL | Full guided behavior not validated. |
| 37 | Errors | extensive dialog_*/toast_* families | Error handling exists in parts | PARTIAL | Reference parity and every error path unverified. |
| 38 | Recovery | restore sample + backup evidence | Recovery foundation exists | PARTIAL | Crash/process-death recovery unverified. |
| 39 | Back behavior | Android activity stack | Navigation/back handling exists | UNVERIFIED | Requires device/instrumented test. |
| 40 | Rotation | Android lifecycle expected | State-saving architecture exists | UNVERIFIED | No emulator execution in current environment. |
| 41 | Relaunch | persisted score expected | Versioned persistence exists | UNVERIFIED | Full process-kill/relaunch test not executable here. |

## Reference-specific evidence

The APK string table explicitly exposes create controls, settings, staff configuration, sharing dialogs, tuplets, wedges, slurs, ties, MIDI/XML dialogs and error toasts. This establishes that these are reference behaviors to test, not invented requirements. Notable examples include `dialog_slur_different_track`, `dialog_wedge_align_error`, `dialog_tuplet`, `toast_tie_cannotbe_played`, `toast_tuplet_spanmeasures`, `settings_scale_editview`, `settings_scale_scoreview`, `staffconfig_up/down/delete/mute`, and `shareimage_*` resources.

## Hard release gates

The following cannot be marked PASS based on source inspection alone:

1. save -> close -> open -> identical score;
2. MusicXML export -> import -> structural equivalence;
3. MIDI event correctness and audible playback;
4. mute suppressing a staff;
5. rotation without data loss;
6. process death/recovery;
7. Android Sharesheet/URI behavior;
8. high-resolution image export;
9. complex-score rendering on multiple form factors.

These require an executable Android build/emulator/device run.
