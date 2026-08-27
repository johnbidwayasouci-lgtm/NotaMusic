#pragma once
#include <lyria/core/core.hpp>
#include <string>
#include <utility>
#include <vector>
namespace lyria::plugins {struct PluginInfo{core::Id id{};std::string name;std::string version;};class Registry{std::vector<PluginInfo>items_;public:void add(PluginInfo p){items_.push_back(std::move(p));}const auto&list()const noexcept{return items_;}};}
