#pragma once
#include <lyria/core/core.hpp>
#include <optional>
#include <string>
#include <vector>
namespace lyria::music { enum class Step{C,D,E,F,G,A,B}; enum class Duration{Whole,Half,Quarter,Eighth,Sixteenth}; enum class Voice{Soprano,Alto,Tenor,Bass,Instrument}; enum class Clef{Treble,Alto,Tenor,Bass}; enum class Dynamic{Ppp,Pp,P,Mp,Mf,F,Ff,Fff}; struct Pitch{Step step{Step::C};int octave{4};int accidental{0};}; struct Lyric{std::string text;std::vector<std::string>phonemes;}; struct Note{core::Id id{};Pitch pitch{};Duration duration{Duration::Quarter};Voice voice{Voice::Soprano};Dynamic dynamic{Dynamic::Mf};bool rest{false};std::optional<Lyric>lyric;}; struct Measure{core::Id id{};int number{};std::vector<Note>notes;}; struct Staff{core::Id id{};std::string name;Clef clef{Clef::Treble};Voice voice{Voice::Soprano};std::vector<Measure>measures;}; struct Score{core::Id id{};std::string title;int tempo{120};int beatsPerBar{4};int beatUnit{4};std::vector<Staff>staves;}; Score make_satb_score(std::string title="Untitled"); }
