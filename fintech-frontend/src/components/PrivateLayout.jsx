import { Outlet } from "react-router-dom";
import NavBar from "./NavBar";

export default function PrivateLayout() {
  return (
    <>
      <NavBar />
      <main className="app-main">
        <Outlet />
      </main>
    </>
  );
}