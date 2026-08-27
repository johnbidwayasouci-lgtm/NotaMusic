#include <lyria/audio/audio.hpp>
#include <algorithm>
namespace lyria::audio {void mix(Buffer&d,const Buffer&s,float g){auto n=std::min(d.samples.size(),s.samples.size());for(std::size_t i=0;i<n;++i)d.samples[i]+=s.samples[i]*g;}}
