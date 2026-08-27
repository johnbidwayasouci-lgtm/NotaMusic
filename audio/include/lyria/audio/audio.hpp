#pragma once
#include <cstddef>
#include <vector>
namespace lyria::audio { struct Buffer{int sampleRate{48000};int channels{2};std::vector<float>samples;void resize(std::size_t n){samples.resize(n);}};void mix(Buffer&,const Buffer&,float gain=1.0f); }
