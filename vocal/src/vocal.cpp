#include <lyria/vocal/vocal.hpp>
#include <cmath>
namespace lyria::vocal {audio::Buffer SimulatedVocalEngine::synthesize(const VocalRequest&r){audio::Buffer b;auto frames=static_cast<std::size_t>(b.sampleRate*r.durationSeconds);b.samples.resize(frames*b.channels);for(std::size_t i=0;i<frames;++i){float x=static_cast<float>(std::sin(2*3.141592653589793*440*i/b.sampleRate)*.08*r.gain);b.samples[2*i]=x;b.samples[2*i+1]=x;}return b;}}
